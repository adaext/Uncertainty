package workflowForMe;

import workflow.ConstraintWTask;
import workflow.WTask;

import java.util.*;

public class Workflow {
    public static class Task {
        enum State {
            EXCUTING,
            READY,
            UNREADY,
            WAIT_FOR_EXCUTING,
            FINISH,
        }

        private String id;
        private int vmID;
        private double excutionTime;
        private double latestStartTime;
        private WTask rawTask;
        private State state;
        private String message;

        public Task(WTask rawTask) {
            this.rawTask = rawTask;
            this.id = rawTask.getTaskId();
            this.excutionTime = rawTask.getBaseExecutionTime();
            state = State.UNREADY;
            latestStartTime = Double.MAX_VALUE;
            message = id + " :UNREADY "+ excutionTime + " ";
        }

        public String getId() {
            return id;
        }

        public int getVmID() {
            return vmID;
        }

        public void setVmID(int vmID) {
            this.vmID = vmID;
        }

        public double getExcutionTime(VM usedVm) {
            return excutionTime * usedVm.getExecutionTimeFactor();
        }

        public double getBaseExcutionTime() {
            return excutionTime ;
        }

        public void setState(State state) {
            this.state = state;
            message += state + " ";
        }

        public String getMessage() {
            return message;
        }

        public State getState() {
            return state;
        }

        public void calLatestStartTime() {
            latestStartTime = Math.min(latestStartTime, getSubDeadline() - rawTask.getBaseExecutionTime());
        }

        public double getSubDeadline() {
            return rawTask.getWTaskSubDeadline();
        }

        public void setSubDeadline(double deadline) {
            rawTask.setWTaskSubDeadline(deadline);
        }

        public void updateSubDeadLine(int workflowDeadLine) {
            double minSuccessorPlst = workflowDeadLine;
            for (Task successor : this.getSuccessorTaskList()) {
                minSuccessorPlst = Math.min(successor.getLatestStartTime(), minSuccessorPlst);
            }
            setSubDeadline(minSuccessorPlst);
        }

        public double getLatestStartTime() {
            return latestStartTime;
        }

        public List<Task> getSuccessorTaskList(){
            List<Task> successorTaskList = new ArrayList<>();
            for (ConstraintWTask wTask : rawTask.getSuccessorTaskList()) {
                successorTaskList.add(taskMap.get(wTask.getWTask().getTaskId()));
            }
            return  successorTaskList;
        }

        public List<Task> getParentTaskList(){
            List<Task> parentTaskList = new ArrayList<>();
            for (ConstraintWTask wTask : rawTask.getParentTaskList()) {
                parentTaskList.add(taskMap.get(wTask.getWTask().getTaskId()));
            }
            return  parentTaskList;
        }

        /**
         * Update successor tasks states and readyTaskList
         */
        public void updateSuccessor() {
            for (Task task : getSuccessorTaskList()) {
                if (task.isAllParentsFinish()) {
                    task.setState(State.READY);
                    readyTaskList.add(task);
                }
            }
        }

        public boolean isAllParentsFinish() {
            if (rawTask.getParentTaskList().size() == 0) {
                return true;
            }
            for (Task parent : getParentTaskList()) {
                if (parent.state != State.FINISH) {
                    return false;
                }
            }
            return true;
        }
    }

    public static List<Task> readyTaskList = new ArrayList<>();
    private workflow.Workflow rawWorkflow;
    private List<Task> taskList = new ArrayList<>();
    private static Map<String, Task> taskMap = new HashMap<>();

    Workflow(workflow.Workflow rawWorkflow) {
        this.rawWorkflow = rawWorkflow;
        for (WTask wTask : rawWorkflow.getTaskList()) {
            Task tempTask = new Task(wTask);
            taskList.add(tempTask);
            taskMap.put(tempTask.id, tempTask);
        }
        // TODO: Calculate lst for each Task
        setPlstForEachTaskPerWorkflow();
    }

    public static int tasksCount = 0;

    private void setPlstForEachTaskPerWorkflow() {
        Queue<Task> queue = getEndQueue();

        while (!queue.isEmpty()) {
            Task head = queue.poll();
            head.updateSubDeadLine(rawWorkflow.getDeadline());
            head.calLatestStartTime();
            // System.out.println("Calculate lst for each Task" + tasksCount++ +" :"+ head.getSubDeadline());
            for (Task task : head.getParentTaskList()) {
                queue.add(task);
            }
        }
    }

    public Queue<Task> getEndQueue() {
        Queue<Task> endQueue = new ArrayDeque<>();
        for (WTask wTask : rawWorkflow.getTaskList()) {
            if (wTask.getSuccessorTaskList().size() == 0) {
                endQueue.add(new Task(wTask));
            }
        }
        return endQueue;
    }

    public int getArrivalTime() {
        return rawWorkflow.getArrivalTime();
    }

    public int getSize() {
        return rawWorkflow.getTaskList().size();
    }
    /**
     * Remove not Ready tasks from readyTaskList.
     */
    public static void clearInvalidTask() {
        Iterator iterator = readyTaskList.iterator();
        while (iterator.hasNext()) {
            Task nextTask = (Task) iterator.next();
            if (nextTask.state != Task.State.READY) {
                iterator.remove();
            }
        }
    }

    public List<Task> getTaskList() {
        return taskList;
    }

    /**
     * Add ready tasks in current workflow to readyTaskList.
     */
    public void addToReadyTaskList() {
        for (Task task : taskList) {
            assert task.state == Task.State.UNREADY : "task is not unready.";
            if (task.isAllParentsFinish()) {
                task.setState(Task.State.READY);
                readyTaskList.add(task);
            }
        }
    }

    //    public List<Task> getSpecifiedTasks(Task.Type type){
//        List<Task> specifiedTasks = new ArrayList<>();
//        for (Task task : taskList) {
//            if (task.getType() == type) {
//                specifiedTasks.add(task);
//            }
//        }
//        return specifiedTasks;
//    }

}
