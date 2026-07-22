package org.example.backend_fundamentals.practice.playground;

@SuppressWarnings({"java:S106", "java:S1118"})
public class Practice {

    private Practice() {
        // Prevent instantiation of utility class
    }

    public static void main(String[] args) throws InterruptedException {
        Running running = new Running();
        Thread a = new Thread(running::run);
        a.start();
        System.out.println("---------------------------GOING TO SLEEP---------------------------");
        Thread.sleep(2000);
        System.out.println("---------------------------FINISH SLEEPING---------------------------");
        running.stop();
//        a.join();
        System.out.println("---------------------------STOPPED---------------------------");
    }

    public static class Running {
        private  boolean shutdown = false;

        void stop() {
            shutdown = true;
        }

        void run() {
            long count = 0;

            while (!shutdown) {
                count++;
            }

            System.out.println("Stopped at " + count);
        }
    }
}
