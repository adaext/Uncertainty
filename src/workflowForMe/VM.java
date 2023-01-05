package workflowForMe;

public class VM {
    public static int currentMaxIndex = 0;
    private boolean isActive;
    private double executionTimeFactor;
    private int id;
    private int level;
    double price;
    private double startTime;
    private double endTime;
    double excutingTaskStartTime;
    private Workflow.Task excutingTask;
    private Workflow.Task waitingTask;
    double predictAvailableTime;


    public VM(double startTime, int level) {
        this.id = currentMaxIndex++;
        this.startTime = startTime;
        this.excutingTaskStartTime = -1;
        this.level = level;
        this.isActive = true;

        switch (level) {
            case 0: //Generate a service instance with the type of t2.xlarge
                this.price = 0.1856;
                this.executionTimeFactor = 1.0;
                break;
            case 1:
                this.price = 0.0928;
                this.executionTimeFactor = 2.0;
                break;
            case 2:
                this.price = 0.0464;
                this.executionTimeFactor = 4.0;
                break;
            case 3:
                this.price = 0.023;
                this.executionTimeFactor = 8.0;
                break;
            default:
                System.out.println("Warming: Only level= 0 1 2 3 are valid!");
        }
    }

    public int getId() {
        return id;
    }

    public Workflow.Task getExcutingTask() {
        return excutingTask;
    }

    public double getEndTime() {
        return endTime;
    }

    public double getStartTime() {
        return startTime;
    }

    public void addTask(Workflow.Task task, double currentTime) {
        assert task.getState() == Workflow.Task.State.READY : "xxxxxxx";
        if (excutingTask == null) {
            excutingTask = task;
            excutingTask.setVmID(id);
            excutingTask.setState(Workflow.Task.State.EXCUTING);
            excutingTaskStartTime = currentTime;
        } else {
            assert waitingTask == null : "VM Not Available";
            waitingTask = task;
            waitingTask.setState(Workflow.Task.State.WAIT_FOR_EXCUTING);
        }
    }

    public void setDeactive(double currentTime){
        // assert isActive == true : "Repeated Deactive";
        if (!isActive()) {
            return;
        }
        isActive = false;
        endTime = currentTime;
    }

    public void NotifyTaskFinish(double currentTime) {
        assert excutingTask != null : "No excuting task";  // 重新分派虚拟状态？
        excutingTask.setState(Workflow.Task.State.FINISH);
        excutingTask = waitingTask;
        if (excutingTask !=null) {
            assert excutingTask.getState() == Workflow.Task.State.WAIT_FOR_EXCUTING;
            excutingTask.setState(Workflow.Task.State.EXCUTING);
            excutingTask.setVmID(id);
            excutingTaskStartTime = currentTime;
        }
        waitingTask = null;
    }

    public boolean isFree() {
        return  excutingTask == null && waitingTask == null;
    }

    public boolean isActive() {
        return isActive;
    }

    public double getExecutionTimeFactor() { return executionTimeFactor; }

    public boolean hasWaitingTask() {
        return waitingTask != null;
    }
}
