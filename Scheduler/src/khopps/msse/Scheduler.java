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
		UserInterface result = new UserInterface()
		{
			private int s_listIndex = 0;

			@Override
			public void run(Scheduler scheduler)
			{
				Workload workload;
				while ((workload = nextWorkload()) != null)
				{
					Task[] schedule = scheduler.makeSchedule(workload);
					if (schedule == null)
						System.out.println("Schedule is not feasible");
					else
					{
						showSchedule(workload, schedule);
						verifySchedule(workload, schedule);
					}
				}
			}

			/*
			 * Sample output:
Schedule for Workload 1
10.2.0.10a  |xx   |     |xx   |     |xx   |     |xx   |     |xx   |     |xx   |     |xx   |     |xx   |     |
10.2.0.10b  |  xx |     |  xx |     |  xx |     |  xx |     |  xx |     |  xx |     |  xx |     |  xx |     |
20.1.5.20   |     |x    |     |     |     |x    |     |     |     |x    |     |     |     |x    |     |     |
20.2.5.20   |     | xx  |     |     |     | xx  |     |     |     | xx  |     |     |     | xx  |     |     |
40.2.5.30a  |     |   xx|     |     |     |     |     |     |     |   xx|     |     |     |     |     |     |
40.2.5.30b  |     |     |    x|x    |     |     |     |     |     |     |    x|x    |     |     |     |     |
80.2.10.60a |     |     |     | xx  |     |     |     |     |     |     |     |     |     |     |     |     |
80.2.10.60b |     |     |     |   xx|     |     |     |     |     |     |     |     |     |     |     |     |
		            0     5     10    15    20    25    30    35    40    45    50    55    60    65    70    75    80    
			 */
			public void showSchedule(Workload workload, Task[] schedule)
			{
				System.out.println("Schedule for " + workload.name());

				Task[] tasks = workload.tasks();
				int hyperperiod = hyperperiod(tasks);

				int maxNameLen = 0;
				String[] lines = new String[tasks.length];
				for (int i = 0; i < tasks.length; ++i)
				{
					Task task = tasks[i];
					StringBuffer sb = new StringBuffer();
					int linePos = 0;
					for (Task t : schedule)
					{
						char c = (t == task) ? 'x' : ' ';
						int n = t.duration();
						for (int j = 0; j < n; ++j)
						{
							if (linePos++ % 5 == 0)
								sb.append('|');
							sb.append(c);
						}
					}
					while (linePos++ < hyperperiod)
						sb.append(' ');
					sb.append('|');

					String name = task.name();
					maxNameLen = Math.max(name.length(), maxNameLen);
					lines[i] = sb.toString();
				}

				String format = "%-" + maxNameLen + "s %s";

				for (int i = 0; i < tasks.length; ++i)
				{
					System.out.println(String.format(format, tasks[i].name(), lines[i]));
				}

				StringBuffer line = new StringBuffer();
				int numLabels = (hyperperiod + 4) / 5 + 1;
				for (int i = 0; i < numLabels; ++i)
				{
					line.append(String.format("%-6d", 5 * i));
				}
				System.out.println(String.format(format, "", line.toString()));
				System.out.println();
			}

			/**
			 * This verifies that the schedule is correct. It is silent if there are no
			 * problems.
			 * 
			 * @param tasks
			 * @param schedule
			 */
			public void verifySchedule(Workload workload, Task[] schedule)
			{
				Task[] tasks = workload.tasks();

				int hyperperiod = hyperperiod(tasks);

				for (int i = 0; i < tasks.length; ++i)
				{
					Task task = tasks[i];
					int nPeriods = hyperperiod / task.period();
					int numPeriods = (int)nPeriods;
					assert numPeriods == nPeriods;
					int numRunsInPeriod[] = new int[numPeriods];
					Arrays.fill(numRunsInPeriod, 0);

					/*
					 * Now run through the schedule, verifying that task[i] is managed
					 * properly. It must run exactly one time in each of its periods,
					 * and it must start and finish within its specified parameters.
					 */
					int now = 0;
					for (Task t : schedule)
					{
						if (t == task)
						{
							int periodIndex = now / t.period(); // which period is this?
							int relativeNow = now % t.period(); // time within the
																// period

							if (relativeNow < t.delay())
								System.out.println("Workload " + workload.name() + ", Task " + t.name()
										+ " launched too early at " + now);

							if (relativeNow + t.duration() > t.deadline())
								System.out.println("Workload " + workload.name() + ", Task " + t.name() + " launched at "
										+ now + " missed deadline");

							assert periodIndex == (int)periodIndex;
							++numRunsInPeriod[(int)periodIndex];
						}
						now += t.duration();
					}

					for (int j = 0; j < numPeriods; ++j)
					{
						if (numRunsInPeriod[j] != 1)
							System.out.println("Workload " + workload.name() + ", Task " + task.name() + " ran "
									+ numRunsInPeriod + " times in period " + j);
					}
				}
			}

			/**
			 * Get the next workload of tasks to be scheduled, or null if there are no
			 * more.
			 * 
			 * @return the next Workload or null
			 */
			public Workload nextWorkload()
			{
				Workload result = null;

				if (++s_listIndex == 1)
				{
					result = new Workload("Workload " + s_listIndex);
					result.add(new Task("10.2.0.10a", 10, 2, 0, 10));
					result.add(new Task("10.2.0.10b", 10, 2, 0, 10));
					result.add(new Task("20.1.5.20", 20, 1, 5, 20));
					result.add(new Task("20.2.5.20", 20, 2, 5, 20));
					result.add(new Task("40.2.5.30a", 40, 2, 5, 30));
					result.add(new Task("40.2.5.30b", 40, 2, 5, 30));
					result.add(new Task("80.2.10.60a", 80, 2, 10, 60));
					result.add(new Task("80.2.10.60b", 80, 2, 10, 60));
				}
				else if (s_listIndex == 2)
				{
					result = new Workload("Workload " + s_listIndex);
					result.add(new Task("20.4.0.15", 20, 4, 0, 15));
					result.add(new Task("20.1.5.20", 20, 1, 5, 20));
					result.add(new Task("30.2.5.30", 30, 2, 5, 30));
					result.add(new Task("30.1.5.30", 30, 1, 5, 30));
					result.add(new Task("50.1.10.40a", 50, 1, 10, 40));
					result.add(new Task("50,1,10.40b", 50, 1, 10, 40));
					result.add(new Task("50.2.25.50a", 50, 2, 25, 50));
					result.add(new Task("50.2.25.50b", 50, 2, 25, 50));
				}
				else if (s_listIndex == 3)
				{
					result = new Workload("Workload " + s_listIndex);
					result.add(new Task("figop", 6000000, 18000));
					result.add(new Task("ament", 6000000, 300));
					result.add(new Task("hinal", 750000, 690000));
					result.add(new Task("figop", 6000000, 18000));
					result.add(new Task("ament", 6000000, 300));
					result.add(new Task("hinal", 750000, 690000));
					result.add(new Task("figop", 6000000, 18000));
					result.add(new Task("ament", 6000000, 300));
					result.add(new Task("hinal", 750000, 690000));
					result.add(new Task("figo", 6000000, 15000));
					result.add(new Task("amcal", 3000000, 24510));
					result.add(new Task("blyjinam", 6000000, 30420));
					result.add(new Task("figwoa", 6000000, 82230));
					result.add(new Task("opent", 6000000, 6390));
					result.add(new Task("entquacal", 6000000, 42060));
					result.add(new Task("gliwoa", 6000000, 51780));
					result.add(new Task("hinwoacalzal", 6000000, 16890));
					result.add(new Task("amcal", 3000000, 24510));
					result.add(new Task("calomir", 1500000, 15000));
					result.add(new Task("amop", 6000000, 15000));
					result.add(new Task("amamqua", 1500000, 15000));
					result.add(new Task("blyfra", 1500000, 60720));
					result.add(new Task("alal", 1500000, 78420));
					result.add(new Task("glihin", 1500000, 23910));
					result.add(new Task("rubzal", 1500000, 35580));
					result.add(new Task("blyfra", 1500000, 60720));
					result.add(new Task("alal", 1500000, 78420));
					result.add(new Task("glihin", 1500000, 23910));
					result.add(new Task("rubzal", 1500000, 35580));
					result.add(new Task("amblywoafrarubrub", 3000000, 288000));
					result.add(new Task("quafragoncalam", 6000000, 355110));
					result.add(new Task("opgonba", 6000000, 66510));
					result.add(new Task("jindelglient", 6000000, 66510));
					result.add(new Task("delrub", 6000000, 66510));
					result.add(new Task("entzal", 6000000, 69510));
					result.add(new Task("rubfigent", 6000000, 98580));
					result.add(new Task("entjinamfigrubgli", 750000, 90600));
					result.add(new Task("amblywoafrarubrub", 3000000, 288000));
					result.add(new Task("quafragoncalam", 6000000, 355110));
					result.add(new Task("opgonba", 6000000, 66510));
					result.add(new Task("jindelglient", 6000000, 66510));
					result.add(new Task("delrub", 6000000, 66510));
					result.add(new Task("entzal", 6000000, 69510));
					result.add(new Task("rubfigent", 6000000, 98580));
					result.add(new Task("entjinamfigrubgli", 750000, 90600));
					result.add(new Task("blyjinam", 6000000, 30420));
					result.add(new Task("figwoa", 6000000, 82230));
					result.add(new Task("opent", 6000000, 6390));
					result.add(new Task("entquacal", 6000000, 42060));
					result.add(new Task("gliwoa", 6000000, 51780));
					result.add(new Task("hinwoacalzal", 6000000, 16890));
					result.add(new Task("blyjinam", 6000000, 30420));
					result.add(new Task("figwoa", 6000000, 82230));
					result.add(new Task("opent", 6000000, 6390));
					result.add(new Task("entquacal", 6000000, 42060));
					result.add(new Task("gliwoa", 6000000, 51780));
					result.add(new Task("hinwoacalzal", 6000000, 16890));
					result.add(new Task("amcal", 3000000, 24510));
					result.add(new Task("blyjinam", 6000000, 30420));
					result.add(new Task("figwoa", 6000000, 82230));
					result.add(new Task("opent", 6000000, 6390));
					result.add(new Task("entquacal", 6000000, 42060));
					result.add(new Task("gliwoa", 6000000, 51780));
					result.add(new Task("hinwoacalzal", 6000000, 16890));
					result.add(new Task("amcal", 3000000, 24510));
					result.add(new Task("blyjinam", 6000000, 30420));
					result.add(new Task("figwoa", 6000000, 82230));
					result.add(new Task("opent", 6000000, 6390));
					result.add(new Task("entquacal", 6000000, 42060));
					result.add(new Task("gliwoa", 6000000, 51780));
					result.add(new Task("hinwoacalzal", 6000000, 16890));
					result.add(new Task("amcal", 3000000, 24510));
					result.add(new Task("figrubmir", 3000000, 22500));
					result.add(new Task("amcal", 3000000, 24510));
					result.add(new Task("blyjinam", 6000000, 30420));
					result.add(new Task("figwoa", 6000000, 82230));
					result.add(new Task("opent", 6000000, 6390));
					result.add(new Task("entquacal", 6000000, 42060));
					result.add(new Task("gliwoa", 6000000, 51780));
					result.add(new Task("hinwoacalzal", 6000000, 16890));
					result.add(new Task("blyjinam", 6000000, 30420));
					result.add(new Task("figwoa", 6000000, 82230));
					result.add(new Task("opent", 6000000, 6390));
					result.add(new Task("entquacal", 6000000, 42060));
					result.add(new Task("gliwoa", 6000000, 51780));
					result.add(new Task("hinwoacalzal", 6000000, 16890));
					result.add(new Task("figrubmir", 3000000, 22500));
					result.add(new Task("figamgon", 1500000, 300000));
					result.add(new Task("figdelo", 6000000, 147000));
					result.add(new Task("amcal", 3000000, 24510));
					result.add(new Task("blyjinam", 6000000, 30420));
					result.add(new Task("figwoa", 6000000, 82230));
					result.add(new Task("opent", 6000000, 6390));
					result.add(new Task("entquacal", 6000000, 42060));
					result.add(new Task("gliwoa", 6000000, 51780));
					result.add(new Task("hinwoacalzal", 6000000, 16890));
					result.add(new Task("amcal", 3000000, 24510));
					result.add(new Task("figop", 6000000, 18000));
					result.add(new Task("ament", 6000000, 300));
					result.add(new Task("hinal", 750000, 690000));
				}

				return result;
			}
		};

		return result;
	}


}
