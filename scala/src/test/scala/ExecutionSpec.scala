package ch01

import org.scalatest._

case class Automatic(override val id: String, override val activities: Set[Activity] = Set.empty,
  override val transitions: Set[Activity] = Set.empty) extends Activity(id, activities, transitions) {

  override def copy(id: String = id, activities: Set[Activity] = activities, transitions: Set[Activity] = transitions): Activity = {
    Automatic(id, activities, transitions)
  }

  override def execute(activityInstance: ActivityInstance): Unit = {
    println(s"Automatic: executing activity $id")
    activityInstance.end()
  }

  override def toString = s"Auto[$id]" + transitions.map("→" + _.id).mkString("")
}

case class Wait(override val id: String, override val activities: Set[Activity] = Set.empty,
  override val transitions: Set[Activity] = Set.empty) extends Activity(id, activities, transitions) {

  override def copy(id: String = id, activities: Set[Activity] = activities, transitions: Set[Activity] = transitions): Activity = {
    Wait(id, activities, transitions)
  }

  override def execute(activityInstance: ActivityInstance): Unit = println(s"Wait: executing activity $id")
}

class ExecutionSpec extends FlatSpec with Matchers {

  "A workflow" should "do sequential automatic execution" in {
    val workflow = new Activity("workflow", Set(Automatic("a"), Automatic("b"))) + ("a" -> "b")
    val workflowInstance = workflow.start()
    assert(openActivities(workflowInstance).isEmpty, "open")
    assert(completedActivities(workflowInstance) == Set("a", "b"), "complete")
  }

  it should "do parallel automatic execution" in {
    val workflow = new Activity("workflow", Set(Automatic("a"), Automatic("b")))
    val workflowInstance = workflow.start()
    assert(openActivities(workflowInstance).isEmpty)
    assert(completedActivities(workflowInstance) == Set("a", "b"))
  }

  it should "do sequential wait execution" in {
    val workflow = new Activity("workflow", Set(Wait("a"), Wait("b"))) + ("a" -> "b")
    val workflowInstance = workflow.start()
    assert(openActivities(workflowInstance) == Set("a"))
    assert(completedActivities(workflowInstance).isEmpty)
  }

  it should "do parallel wait execution" in {
    val workflow = new Activity("workflow", Set(Wait("a"), Wait("b")))
    val workflowInstance = workflow.start()
    assert(openActivities(workflowInstance) == Set("a", "b"))
    assert(completedActivities(workflowInstance).isEmpty)
  }

  private def openActivities(workflowInstance: ActivityInstance): Set[String] = {
    workflowInstance.activityInstances.filter(_.endTime.isEmpty).map(_.activity.id).toSet
  }

  private def completedActivities(workflowInstance: ActivityInstance): Set[String] = {
    workflowInstance.activityInstances.filter(_.endTime.isDefined).map(_.activity.id).toSet
  }
}
