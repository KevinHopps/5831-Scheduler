package khopps.msse;

import java.util.ArrayList;

/**
 * This class represents a workload, a list of Tasks to be scheduled.
 * 
 * @author Kevin
 * 
 */
public class Workload
{
	private final String m_name;
	private ArrayList<Task> m_tasks = new ArrayList<Task>();
	private Task[] m_cache = null;

	public Workload(String name)
	{
		m_name = name;
	}

	public String name()
	{
		return m_name;
	}

	public void add(Task task)
	{
		m_tasks.add(task);
		m_cache = null;
	}

	public Task[] tasks()
	{
		if (m_cache == null)
		{
			m_cache = new Task[m_tasks.size()];
			m_tasks.toArray(m_cache);
		}

		return m_cache;
	}
}
