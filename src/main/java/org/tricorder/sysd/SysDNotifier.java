package org.tricorder.sysd;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

public class SysDNotifier {
		private long timeout;
		private Auditor auditor=null;
		private Scheduler scheduler;
		private JobDetail job;

		public SysDNotifier() {
			String watchdogUsec = System.getenv().get("WATCHDOG_USEC");
			if (watchdogUsec!=null) {
				timeout = Long.parseLong(watchdogUsec);
				timeout = timeout / 1000; //convert from micro to milliseconds
			}
		}
			
		
		public SysDNotifier(boolean checkEnv) throws SysDNotifierEnvError {
			if (checkEnv) {				
				try {
					String watchdogUsec = System.getenv().get("WATCHDOG_USEC");
					if (watchdogUsec!=null) {
						timeout = Long.parseLong(watchdogUsec);
						timeout = timeout / 1000; //convert from micro to milliseconds
					}
				}
				catch (NumberFormatException nfe) {
					throw new SysDNotifierEnvError();
				}				
			}
		}	
		
		
		public void setTimeout(long millis) {
			this.timeout = millis;
		}
	
		public void setAuditor(Auditor auditor) {
			this.auditor = auditor;
			job.getJobDataMap().put("auditor", auditor);
		}
		
		public void once() throws SysDNotifierLibError {
			if (auditor==null) {
            	auditor = new Auditor() {
					@Override
					public boolean allGood() {
						return true;
					}	            		
            	};
			}
			if (auditor.allGood()) {
				if (!new c.sysdnotify().pingWatchdog().equals("Notified")) {	
					throw new SysDNotifierLibError();					
				}
			}
		}
		
		public void start() {
	        try {
	        	if (timeout>0) {
		            // Grab the Scheduler instance from the Factory
		            scheduler = StdSchedulerFactory.getDefaultScheduler();
		            // and start it off
		            scheduler.start();
		            job = newJob(WatchdogJob.class)
		            	    .withIdentity("job1", "group1")
		            	    .build();
		            
		            if (auditor==null) {
		            	auditor = new Auditor() {
							@Override
							public boolean allGood() {
								return true;
							}	            		
		            	};
		            }
		            
		            job.getJobDataMap().put("auditor", auditor);
	
		            	// Trigger the job to run now, and then repeat every timeout/2 milliseconds
		            	Trigger trigger = newTrigger()
		            	    .withIdentity("trigger1", "group1")
		            	    .startNow()
		            	    .withSchedule(simpleSchedule()
		            	            .withIntervalInMilliseconds(timeout/2)
		            	            .repeatForever())
		            	    .build();
	
		            	// Tell quartz to schedule the job using our trigger
		            	scheduler.scheduleJob(job, trigger);

	            }

	        } catch (SchedulerException se) {
	            se.printStackTrace();
	        }

		}

}
