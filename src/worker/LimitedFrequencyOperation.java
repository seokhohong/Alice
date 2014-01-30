package worker;

/** Used for tasks that are costly to run and can run on a timed basis */
public interface LimitedFrequencyOperation 
{
	public boolean shouldUpdate();
	public void doWork();
}
