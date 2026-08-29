package com.debzg.gotasks.presentation.tasks

import com.debzg.gotasks.domain.model.Task
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * GoTasks doesn't create or re-parent subtasks, but existing ones arrive from Google Tasks and
 * still have to render under the right parent.
 */
class SubtaskOrderingTest {

  private fun task(id: String, position: String, parentId: String? = null) =
    Task(
      id = id,
      taskListId = "list",
      title = id,
      notes = null,
      isCompleted = false,
      due = null,
      completedAt = null,
      parentId = parentId,
      position = position,
    )

  @Test
  fun `subtasks follow their parent regardless of position ordering`() {
    // Subtask positions are scoped to the parent, so sorting the flat list by position alone
    // would scatter this child far from the task it belongs to.
    val parent = task("parent", "00001")
    val other = task("other", "00002")
    val child = task("child", "99999", parentId = "parent")

    val ordered = orderWithSubtasks(listOf(other, child, parent))

    assertEquals(listOf("parent", "child", "other"), ordered.map { it.id })
  }

  @Test
  fun `multiple subtasks stay in position order under their parent`() {
    val ordered =
      orderWithSubtasks(
        listOf(task("b", "00002", parentId = "parent"), task("parent", "00001"), task("a", "00001", parentId = "parent"))
      )

    assertEquals(listOf("parent", "a", "b"), ordered.map { it.id })
  }

  @Test
  fun `a subtask whose parent is absent is kept as top level`() {
    // The parent may be completed or not yet pulled — the child must not disappear.
    val orphan = task("orphan", "00002", parentId = "missing")
    val normal = task("normal", "00001")

    val ordered = orderWithSubtasks(listOf(orphan, normal))

    assertEquals(listOf("normal", "orphan"), ordered.map { it.id })
  }
}
