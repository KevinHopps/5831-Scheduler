package khopps.msse;

import java.util.Arrays;

public class TextUI implements UserInterface
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
		int hyperperiod = Scheduler.hyperperiod(tasks);

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

		int hyperperiod = Scheduler.hyperperiod(tasks);

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
		/*
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
		*/

		return result;
	}

}
