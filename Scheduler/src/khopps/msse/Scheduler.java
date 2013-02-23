package khopps.msse;

import java.util.Arrays;
import java.util.Stack;

/**
 * Scheduler will generate a static schedule for a set of tasks.
 * 
 * @author khopps
 * 
 */
public class Scheduler
{
	
	public static int hyperperiod(Task[] tasks)
	{
		int hyperperiod = tasks[0].period();
		for (int i = 1; i < tasks.length; ++i)
			hyperperiod = MathUtils.lcm((int)tasks[i].period(), hyperperiod);

		return hyperperiod;
	}

	/**
	 * This will make a schedule, if possible, for the given tasks. The schedule
	 * last for one hyperperiod, the least common multiple of all of the
	 * individual task periods.
	 * 
	 * @param tasks
	 * @return the tasks, in order of their execution for the hyperperiod, or
	 *         null if the tasks cannot be scheduled.
	 */
	public Task[] makeSchedule(Workload workload)
	{
		Stack<Task> schedule = null;

		Task[] tasks = workload.tasks();

		if (tasks.length > 0)
		{
			int hyperperiod = hyperperiod(tasks);

			/*
			 * Now attempt to schedule the task, using the recursive helper
			 * function makeSchedule().
			 */
			schedule = new Stack<Task>();
			if (!makeSchedule(schedule, workload, 0, hyperperiod))
				schedule = null;
		}

		Task[] result = null;
		if (schedule != null)
		{
			result = new Task[schedule.size()];
			schedule.toArray(result);
		}

		return result;
	}

	/**
	 * This is a recursive function that will attempt to schedule tasks from now
	 * until the end of a certain period of time.
	 * 
	 * @param schedule
	 *            is the schedule as it exists thus far, and the result if
	 *            successful.
	 * @param tasks
	 *            is the set of Tasks that need to fill the schedule.
	 * @param now
	 *            is the current time within the schedule.
	 * @param end
	 *            is the time when the schedule ends.
	 * @return true if the schedule is possible, false if not.
	 */
	private boolean makeSchedule(Stack<Task> schedule, Workload workload, int now, int end)
	{
		boolean result = false;

		Task[] tasks = workload.tasks();
		Task[] choices = new Task[tasks.length];

		/*
		 * First, get a list of choices -- tasks which are eligible to run at
		 * this time. If no tasks are eligible now, a single Idle task is
		 * returned, which is not one of the original set.
		 * 
		 * Given the list of choices, we make the first choice and see if we can
		 * then schedule the remaining tasks. If so, we are finished. If not, we
		 * undo that choice and try the next choice.
		 */
		int numChoices = getChoices(choices, tasks, now); // What tasks are eligible to run now.
		boolean done = false;
		for (int i = 0; !done && i < numChoices; ++i)
		{
			Task task = choices[i];
			schedule.push(task); // Add this task to the schedule.
			int previousLaunch = task.launch(now); // Record this task's launch time.
			int finish = now + task.duration();
			if (finish >= end) // Are we done making the schedule?
			{
				result = true; // success!
				done = true;
			}
			else
			{
				/*
				 * Now we recurse, attempting to finish the remainder of the
				 * schedule. If successful, we're done. If unsuccessful, we
				 * undo our choice and try again in the next loop.
				 */
				result = makeSchedule(schedule, workload, finish, end);
				if (result)
					done = true;
				else
				{
					/*
					 * Undo our choice by popping it off the end of the
					 * schedule and resetting the tasks previous launch time
					 * to what it was before. This is so that it can
					 * correctly calculate its next deadline.
					 */
					schedule.pop();
					task.launch(previousLaunch);
				}
			}
		}

		return result;
	}

	/**
	 * Given a set of tasks, return an array of them which are eligible to run
	 * at this time. The array will be sorted by earliest deadline first. If no
	 * tasks from the list are eligible now, an array consisting of a single
	 * Idle task will be returned.
	 * 
	 * @param tasks
	 *            the pool of tasks that is scanned for eligibility
	 * @param now
	 *            is the current time
	 * @return an array of tasks that are eligible to run now. Never null.
	 */
	private int getChoices(Task[] choices, Task[] tasks, int now)
	{
		int numChoices = 0;

		/*
		 * We loop through all the tasks. Each one that is eligible will be
		 * added to taskVec. Along the way, the minimum wait time until the next
		 * task may run is maintained. After the loop, if the minimum wait time
		 * is positive, it means that no tasks are eligible to run at this time.
		 */
		int minWait = Integer.MAX_VALUE;
		for (int i = 0; i < tasks.length; ++i)
		{
			Task task = tasks[i];
			int wait = task.mustWait(now);
			if (minWait > wait)
				minWait = wait;
			/*
			 * If wait==0, the task is eligible to run. However, if it will
			 * finish after its deadline, there's no use returning it as a
			 * choice.
			 */
			if (wait == 0 && now + task.duration() <= task.nextDeadline())
				choices[numChoices++] = task;
		}

		/*
		 * If no tasks are eligible to run, return a single Idle task, the
		 * duration of which is the waiting time until the next eligible task
		 * may run.
		 */
		if (minWait > 0)
		{
			choices[numChoices++] = Task.newIdleTask(minWait);
		}
		
		if (numChoices > 0)
		{
			Arrays.sort(choices, 0, numChoices);
		}
		
		return numChoices;
	}

	public static void main(String[] args)
	{
		Scheduler scheduler = new Scheduler();
		UserInterface ui = newUI();
		ui.run(scheduler);
	}
	
	private static UserInterface newUI()
	{
		UserInterface result = new TextUI();

		return result;
	}


}
