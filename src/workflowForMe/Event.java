package workflowForMe;

public class Event {
    enum Type{
        WORKFLOW_ARRIVE,
        TASK_FINISH,
        TASK_ACHIEVE_PLST,
        VM_CHECK,
        SCHEDULE,
    }
    double timeStamp;
    Type type;
    Workflow workflow;
    Workflow.Task task;
    VM vm;

    public Event(Type type, double timeStamp) {
        this.type = type;
        this.timeStamp = timeStamp;
    }

    public Event(double arrivalTime, Type workflowType, Workflow workflow) {
        timeStamp = arrivalTime;
        type = workflowType;
        this.workflow = workflow;
    }

    public Event(double timeStamp, Type workflowType, Workflow.Task task) {
        this.timeStamp = timeStamp;
        type = workflowType;
        this.task = task;
    }

    public Event(double timeStamp, Type workflowType, VM vm) {
        this.timeStamp = timeStamp;
        type = workflowType;
        this.vm = vm;
    }

    public Event(double timeStamp, Type workflowType, Workflow.Task task, VM vm) {
        this.timeStamp = timeStamp;
        type = workflowType;
        this.task = task;
        this.vm = vm;
    }

}
