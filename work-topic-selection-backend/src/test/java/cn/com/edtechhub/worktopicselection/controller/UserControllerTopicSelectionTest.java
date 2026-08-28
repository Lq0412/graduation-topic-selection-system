package cn.com.edtechhub.worktopicselection.controller;

import cn.com.edtechhub.worktopicselection.constant.TopicConstant;
import cn.com.edtechhub.worktopicselection.exception.BusinessException;
import cn.com.edtechhub.worktopicselection.mapper.StudentTopicSelectionMapper;
import cn.com.edtechhub.worktopicselection.mapper.TopicMapper;
import cn.com.edtechhub.worktopicselection.mapper.UserMapper;
import cn.com.edtechhub.worktopicselection.model.entity.StudentTopicSelection;
import cn.com.edtechhub.worktopicselection.model.entity.Topic;
import cn.com.edtechhub.worktopicselection.model.entity.User;
import cn.com.edtechhub.worktopicselection.model.enums.StudentTopicSelectionStatusEnum;
import cn.com.edtechhub.worktopicselection.model.enums.TopicStatusEnum;
import cn.com.edtechhub.worktopicselection.model.enums.UserRoleEnum;
import cn.com.edtechhub.worktopicselection.response.BaseResponse;
import cn.com.edtechhub.worktopicselection.service.StudentTopicSelectionService;
import cn.com.edtechhub.worktopicselection.service.SwitchService;
import cn.com.edtechhub.worktopicselection.service.TopicService;
import cn.com.edtechhub.worktopicselection.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTopicSelectionTest {

    private final UserController controller = new UserController();

    @Mock
    private UserMapper userMapper;

    @Mock
    private TopicMapper topicMapper;

    @Mock
    private StudentTopicSelectionMapper selectionMapper;

    @Mock
    private UserService userService;

    @Mock
    private TopicService topicService;

    @Mock
    private StudentTopicSelectionService selectionService;

    @Mock
    private SwitchService switchService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(controller, "userMapper", userMapper);
        ReflectionTestUtils.setField(controller, "topicMapper", topicMapper);
        ReflectionTestUtils.setField(controller, "studentTopicSelectionMapper", selectionMapper);
        ReflectionTestUtils.setField(controller, "userService", userService);
        ReflectionTestUtils.setField(controller, "topicService", topicService);
        ReflectionTestUtils.setField(controller, "studentTopicSelectionService", selectionService);
        ReflectionTestUtils.setField(controller, "switchService", switchService);
    }

    @Test
    void topicOwnershipAndReviewTransitionsAreBoundToActor() {
        User teacher = user(1L, "teacher-1", "张老师", "计算机系", UserRoleEnum.TEACHER);
        User otherTeacher = user(2L, "teacher-2", "张老师", "计算机系", UserRoleEnum.TEACHER);
        User dept = user(3L, "dept-1", "王主任", "计算机系", UserRoleEnum.DEPT);
        User otherDept = user(4L, "dept-2", "赵主任", "外语系", UserRoleEnum.DEPT);
        Topic topic = publishedTopic(10L, 1);
        topic.setTeacherName(teacher.getUserName());
        topic.setTeacherAccount(teacher.getUserAccount());
        topic.setDeptName(teacher.getDept());

        assertTrue(controller.isTopicOwner(teacher, topic));
        assertFalse(controller.isTopicOwner(otherTeacher, topic));

        topic.setStatus(TopicStatusEnum.PENDING_REVIEW.getCode());
        assertTrue(controller.isAllowedTopicStatusTransition(dept, topic, TopicStatusEnum.NOT_PUBLISHED));
        assertTrue(controller.isAllowedTopicStatusTransition(dept, topic, TopicStatusEnum.REJECTED));
        assertFalse(controller.isAllowedTopicStatusTransition(otherDept, topic, TopicStatusEnum.REJECTED));
        assertFalse(controller.isAllowedTopicStatusTransition(teacher, topic, TopicStatusEnum.NOT_PUBLISHED));

        topic.setStatus(TopicStatusEnum.REJECTED.getCode());
        assertTrue(controller.isAllowedTopicStatusTransition(teacher, topic, TopicStatusEnum.PENDING_REVIEW));
        assertFalse(controller.isAllowedTopicStatusTransition(otherTeacher, topic, TopicStatusEnum.PENDING_REVIEW));
    }

    @Test
    void removingPreselectionDoesNotInflateCapacity() {
        Topic topic = publishedTopic(10L, 2);
        topic.setSelectAmount(1);
        StudentTopicSelection preselection = selection(20L, "student-1", topic.getId(), StudentTopicSelectionStatusEnum.EN_PRESELECT);

        controller.restoreTopicCounters(topic, preselection);

        assertEquals(2, topic.getSurplusQuantity());
        assertEquals(0, topic.getSelectAmount());

        StudentTopicSelection finalSelection = selection(21L, "student-1", topic.getId(), StudentTopicSelectionStatusEnum.EN_SELECT);
        controller.restoreTopicCounters(topic, finalSelection);
        assertEquals(3, topic.getSurplusQuantity());
        assertEquals(0, topic.getSelectAmount());
    }

    @Test
    void confirmSelectionLocksStudentBeforeTopicAndConsumesCapacityOnce() {
        User student = user(1L, "student-1", "学生甲", "计算机系", UserRoleEnum.STUDENT);
        Topic topic = publishedTopic(10L, 1);
        StudentTopicSelection preselection = selection(20L, student.getUserAccount(), topic.getId(), StudentTopicSelectionStatusEnum.EN_PRESELECT);

        when(userMapper.selectByIdForUpdate(student.getId())).thenReturn(student);
        when(userService.userIsStudent(student)).thenReturn(true);
        when(topicMapper.selectByIdForUpdate(topic.getId())).thenReturn(topic);
        when(switchService.isEnabled(TopicConstant.SWITCH_SINGLE_CHOICE)).thenReturn(true);
        when(switchService.isEnabled(TopicConstant.CROSS_TOPIC_SWITCH)).thenReturn(false);
        when(selectionMapper.selectByUserForUpdate(student.getUserAccount())).thenReturn(Collections.singletonList(preselection));
        when(selectionService.updateById(preselection)).thenReturn(true);
        when(topicService.updateById(topic)).thenReturn(true);

        BaseResponse<Long> response = controller.confirmStudentSelection(student, topic.getId());

        assertEquals(preselection.getId(), response.getData());
        assertEquals(StudentTopicSelectionStatusEnum.EN_SELECT.getCode(), preselection.getStatus());
        assertEquals(0, topic.getSurplusQuantity());
        verify(selectionService, never()).save(any(StudentTopicSelection.class));
        InOrder lockOrder = inOrder(userMapper, topicMapper, selectionMapper);
        lockOrder.verify(userMapper).selectByIdForUpdate(student.getId());
        lockOrder.verify(topicMapper).selectByIdForUpdate(topic.getId());
        lockOrder.verify(selectionMapper).selectByUserForUpdate(student.getUserAccount());
    }

    @Test
    void finalSelectionOnAnotherTopicBlocksSecondChoice() {
        User student = user(1L, "student-1", "学生甲", "计算机系", UserRoleEnum.STUDENT);
        Topic topic = publishedTopic(10L, 1);
        StudentTopicSelection currentPreselection = selection(20L, student.getUserAccount(), topic.getId(), StudentTopicSelectionStatusEnum.EN_PRESELECT);
        StudentTopicSelection existingFinal = selection(21L, student.getUserAccount(), 11L, StudentTopicSelectionStatusEnum.EN_SELECT);

        when(userMapper.selectByIdForUpdate(student.getId())).thenReturn(student);
        when(userService.userIsStudent(student)).thenReturn(true);
        when(topicMapper.selectByIdForUpdate(topic.getId())).thenReturn(topic);
        when(switchService.isEnabled(TopicConstant.SWITCH_SINGLE_CHOICE)).thenReturn(true);
        when(switchService.isEnabled(TopicConstant.CROSS_TOPIC_SWITCH)).thenReturn(false);
        when(selectionMapper.selectByUserForUpdate(student.getUserAccount())).thenReturn(Arrays.asList(currentPreselection, existingFinal));

        assertThrows(BusinessException.class, () -> controller.confirmStudentSelection(student, topic.getId()));
        assertEquals(1, topic.getSurplusQuantity());
        verify(selectionService, never()).updateById(any(StudentTopicSelection.class));
        verify(topicService, never()).updateById(any(Topic.class));
    }

    @Test
    void teacherAssignmentReusesExistingPreselection() {
        User teacher = user(1L, "teacher-1", "张老师", "计算机系", UserRoleEnum.TEACHER);
        User student = user(2L, "student-1", "学生甲", "计算机系", UserRoleEnum.STUDENT);
        Topic topic = publishedTopic(10L, 1);
        topic.setTeacherName(teacher.getUserName());
        topic.setTeacherAccount(teacher.getUserAccount());
        topic.setDeptName(teacher.getDept());
        StudentTopicSelection preselection = selection(20L, student.getUserAccount(), topic.getId(), StudentTopicSelectionStatusEnum.EN_PRESELECT);

        when(userMapper.selectByIdForUpdate(student.getId())).thenReturn(student);
        when(userService.userIsStudent(student)).thenReturn(true);
        when(topicMapper.selectByIdForUpdate(topic.getId())).thenReturn(topic);
        when(selectionMapper.selectByUserForUpdate(student.getUserAccount())).thenReturn(Collections.singletonList(preselection));
        when(selectionService.updateById(preselection)).thenReturn(true);
        when(topicService.updateById(topic)).thenReturn(true);

        controller.assignStudentSelection(teacher, student, topic.getId());

        assertEquals(StudentTopicSelectionStatusEnum.EN_SELECT.getCode(), preselection.getStatus());
        assertEquals(0, topic.getSurplusQuantity());
        assertEquals(0, topic.getSelectAmount());
        verify(selectionService, never()).save(any(StudentTopicSelection.class));
        InOrder lockOrder = inOrder(userMapper, topicMapper, selectionMapper);
        lockOrder.verify(userMapper).selectByIdForUpdate(student.getId());
        lockOrder.verify(topicMapper).selectByIdForUpdate(topic.getId());
        lockOrder.verify(selectionMapper).selectByUserForUpdate(student.getUserAccount());
    }

    private static User user(Long id, String account, String name, String dept, UserRoleEnum role) {
        User user = new User();
        user.setId(id);
        user.setUserAccount(account);
        user.setUserName(name);
        user.setDept(dept);
        user.setUserRole(role.getCode());
        return user;
    }

    private static Topic publishedTopic(Long id, int surplus) {
        Topic topic = new Topic();
        topic.setId(id);
        topic.setStatus(TopicStatusEnum.PUBLISHED.getCode());
        topic.setDeptName("计算机系");
        topic.setSurplusQuantity(surplus);
        topic.setSelectAmount(0);
        topic.setStartTime(new Date(System.currentTimeMillis() - 60_000));
        topic.setEndTime(new Date(System.currentTimeMillis() + 60_000));
        return topic;
    }

    private static StudentTopicSelection selection(
            Long id,
            String account,
            Long topicId,
            StudentTopicSelectionStatusEnum status
    ) {
        StudentTopicSelection selection = new StudentTopicSelection();
        selection.setId(id);
        selection.setUserAccount(account);
        selection.setTopicId(topicId);
        selection.setStatus(status.getCode());
        return selection;
    }
}
