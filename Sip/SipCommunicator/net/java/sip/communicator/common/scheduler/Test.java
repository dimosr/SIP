package net.java.sip.communicator.common.scheduler;

/**
 * <p>Title: SIP Communicator</p>
 * <p>Description: A SIP UA</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Network Research Team, Louis Pasteur University</p>
 * @author Emil Ivov
 * @version 1.0
 */

public class Test
{
    Timer timer = new Timer(false);
    public Test()
    {
        Task t1 = new Task("T1");
        Task t2 = new Task("T2");
        Task t3 = new Task("T3");
        System.out.println("TT: time is "+System.currentTimeMillis());
        timer.schedule(t1, new java.util.Date(System.currentTimeMillis() + 3000));
        timer.schedule(t2,  4000);
        timer.schedule(t3, new java.util.Date(System.currentTimeMillis() + 10000));
        timer.reschedule(t1, new java.util.Date(System.currentTimeMillis() + 5000));
        timer.reschedule(t3, new java.util.Date(System.currentTimeMillis() + 1000));
    }
    public static void main(String[] args)
    {
        Test test1 = new Test();
    }

    public class Task extends TimerTask
    {
        String name = null;

        Task(String n)
        {name = n;}
        public void run()
        {
            System.out.println(name +": time is "+System.currentTimeMillis());
        }
    }

}
