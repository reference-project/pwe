package ch01

import java.util.Date
import scala.collection.mutable.ListBuffer


/** A definition of an execution flow, composed of activities (nodes) in the process, and their transitions. */
case class Workflow(activities: Set[Activity] = Set.empty) {

  /** Returns a copy of this workflow with the transition specified by the pair of activity IDs added.*/
  def +(transition: (String, String)): Workflow = transition match { case(fromId, toId) =>
    (for {
      from <- activities.find(_.id == fromId)
      to <- activities.find(_.id == toId)
    } yield (from, to)).map { case (from, to) =>
      val fromWithTransition = from.copy(transitions = from.transitions + Transition(from, to))
      copy(activities = activities.filterNot(_.id == fromId) + fromWithTransition)
    }.getOrElse(this)
  }
}


/** A sequence flow from one workflow activity to another. */
case class Transition(from: Activity, to: Activity)


/** A static description of one step in a workflow, with a unique identifier for establishing transitions, and a set of
  * outgoing transitions. Subclasses implement concrete runtime behaviour. */
abstract class Activity(val id: String, val transitions: Set[Transition] = Set.empty) {

  def copy(id: String = id, transitions: Set[Transition] = transitions): Activity

  def execute(activityInstance: ActivityInstance): Unit
}


/** One execution of a workflow. */
case class WorkflowInstance(workflow: Workflow, activityInstances: ListBuffer[ActivityInstance] = new ListBuffer()) {

  /** Starts execution of this workflow, by executing activities with no incoming transition. */
  def start(): WorkflowInstance = {
    val toActivities = workflow.activities.flatMap(_.transitions).map(_.to)
    val startActivities = workflow.activities.diff(toActivities)
    startActivities.foreach(startActivity => execute(startActivity))
    this
  }

  /** Executes an activity within this workflow instance. */
  def execute(activity: Activity): Unit = {
    val activityInstance = ActivityInstance(activity, this)
    activityInstances.append(activityInstance)
    activity.execute(activityInstance)
  }
}


/** A single case of running an activity. */
case class ActivityInstance(activity: Activity, workflowInstance: WorkflowInstance, var endTime: Option[Date] = None) {

  val startTime = new Date()

  /** Completes this activity instance and continues execution via all outgoing transitions to the next activities. */
  def end(): Unit = {
    endTime = Some(new Date())
    activity.transitions.foreach(transition => take(transition))
  }

  /** Completes this activity instance and continues execution via one transition to one next activity. */
  def take(transition: Transition): Unit = {
    endTime = Some(new Date())
    workflowInstance.execute(transition.to)
  }
}
