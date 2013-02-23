package khopps.msse;

/**
 * Task contains the specifications for the task, including its period, its
 * delay (release time), its duration (WCET) and its deadline. In addition, the
 * Task keeps track of when it was last launched
 * 
 * @author khopps
 * 
 */
public class Task implements Comparable<Task>
{
	private final String m_name;
	private final int m_period;
	private final int m_delay;
	private final int m_duration;
	private final int m_deadline;
	private int m_lastLaunch = -1;
	private static final String IDLE = "Idle"; // special name for Idle task.

	public static Task newIdleTask(int duration)
	{
		return new Task(IDLE, duration, duration, 0, Integer.MAX_VALUE);
	}

	public Task(String name, int period, int duration)
	{
		this(name, period, duration, 0, period);
	}

	public Task(String name, int period, int duration, int delay, int deadline)
	{
		m_name = name;
		m_period = period;
		m_delay = delay;
		m_duration = duration;
		m_deadline = deadline;

		m_lastLaunch = -m_period; // to make nextDeadline work out ok
	}

	public boolean isIdleTask()
	{
		return m_name == IDLE;
	}

	public String name()
	{
		return m_name;
	}

	public int period()
	{
		return m_period;
	}

	public int delay()
	{
		return m_delay;
	}

	public int duration()
	{
		return m_duration;
	}

	public int deadline()
	{
		return m_deadline;
	}

	/**
	 * This calculates the next deadline based on the last launch time.
	 * 
	 * @return
	 */
	public int nextDeadline()
	{
		int result = Integer.MAX_VALUE;
		if (!isIdleTask())
		{
			int periodOfNextLaunch = (m_lastLaunch + m_period) / m_period;
			result = periodOfNextLaunch * m_period + m_deadline;
		}
		return result;
	}

	/**
	 * This set the last launch time
	 * 
	 * @param now
	 * @return the previous launch time
	 */
	public int launch(int now)
	{
		int result = m_lastLaunch;
		m_lastLaunch = now;
		return result;
	}

	/**
	 * Calculate the time this task must wait before being launched. A result of
	 * zero means it is eligible now.
	 * 
	 * @param now
	 * @return time the task must wait for being launched
	 */
	public int mustWait(int now)
	{
		int periodStart = (now / m_period) * m_period;
		int earliestLaunch = periodStart + m_delay;
		if (m_lastLaunch >= earliestLaunch)
			earliestLaunch += m_period;
		int result = Math.max(0, earliestLaunch - now);
		return result;
	}

	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer();

		sb.append('{');
		sb.append("name=");
		sb.append(m_name);
		sb.append(", period=");
		sb.append(m_period);
		sb.append(", delay=");
		sb.append(m_delay);
		sb.append(", duration=");
		sb.append(m_duration);
		sb.append(", deadline=");
		sb.append(m_deadline);
		sb.append(", launch=");
		sb.append(m_lastLaunch);
		sb.append('}');

		String result = sb.toString();
		return result;
	}

	@Override
	public int compareTo(Task other)
	{
		return this.nextDeadline() - other.nextDeadline();
	}

}
