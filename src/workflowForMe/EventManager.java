package workflowForMe;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class EventManager {
    PriorityQueue<Event> priorityQueue = new PriorityQueue<>(new Comparator<Event>() {
        @Override
        public int compare(Event o1, Event o2) {
            if (o1.timeStamp == o2.timeStamp) {
                return 0;
            }
            return o1.timeStamp < o2.timeStamp ? -1 : 1;
        }
    });

    VMManager vmManager = new VMManager();

    public void addEvent(Event event) {
        // System.out.println(event.type + " " + event.timeStamp + " added");
        priorityQueue.add(event);
    }

    public boolean hasEvent() {
        return !priorityQueue.isEmpty();
    }

    public void consumeEvent() {
        Event event = consumeEventFromPriorityQueue();
        System.out.println(event.type + " start: " + event.timeStamp);
        if (event.type == Event.Type.WORKFLOW_ARRIVE) {
            processWorkflowArrive(event.workflow);
        } else if (event.type == Event.Type.TASK_FINISH) {
            processTaskFinish(event.timeStamp, event.workflow, event.task, event.vm);
            // System.out.println("TASK_FINISH");
        } else if (event.type == Event.Type.TASK_ACHIEVE_PLST) {
            processTaskAchievePlst(event.task, event.timeStamp);
            // System.out.println("TASK_ACHIEVE_PLST");
        } else if (event.type == Event.Type.VM_CHECK) {
            processVMCheck(event.vm, event.timeStamp);
        } else if (event.type == Event.Type.SCHEDULE) {
            processEventSchedule(event.timeStamp);
            // System.out.println("SCHEDULE");
        } else {
            assert false:"Undefined type";
        }
        System.out.println(event.type + " finish: " + event.timeStamp);
    }

    private void processEventSchedule(double currentTime) {
        // TODO:
        if (vmManager.getActiveList().size() == 0) {
            for (int i = 0; i < 5; i++) {
                vmManager.addNewActiveVM(new VM(currentTime, i % 4));
            }
        }
        List<Workflow.Task> readyTaskList = Workflow.readyTaskList;

        for(Workflow.Task readyTask : readyTaskList) {
            for (VM activeVM : vmManager.getActiveList()) {
                if (!activeVM.hasWaitingTask()) {
                    activeVM.addTask(readyTask, currentTime);
                    if (readyTask.getState() == Workflow.Task.State.EXCUTING) {
                        addEvent(new Event(currentTime + readyTask.getExcutionTime(activeVM), Event.Type.TASK_FINISH, readyTask, activeVM));
                    }
                    break;
                }
            }
        }
        Workflow.clearInvalidTask();
    }

    private void processVMCheck(VM vm, double currentTime) {
        // TODO: check and free VM
//        检查对应虚拟机状态
//                视情况关闭虚拟机
        vmManager.checkAndRemoveVm(vm, currentTime);

    }

    private void processTaskAchievePlst(Workflow.Task task, double currentTime) {
        assert task.getState() != Workflow.Task.State.UNREADY: "Tasks couldn't be unready";
        if (task.getState() != Workflow.Task.State.READY) {
            return;
        }

        // TODO: chose an optimal VM
        VM newVM = new VM(currentTime, 0);
        newVM.addTask(task, currentTime);
        vmManager.addNewActiveVM(newVM);
        addEvent(new Event(currentTime + task.getExcutionTime(newVM), Event.Type.TASK_FINISH, task, newVM));
    }

    private void processTaskFinish(double currentTime, Workflow workflow, Workflow.Task task, VM vm) {
        //          task 对应虚拟机更新状态
//          task 对应的 successor tasks 的状态
//          新建 Schedule event
//          如果没有 wait task，新增一个 VM check 的 event
        vm.NotifyTaskFinish(currentTime);
        if (vm.getExcutingTask() != null) {
            addEvent(new Event(currentTime + vm.getExcutingTask().getExcutionTime(vm),
                               Event.Type.TASK_FINISH,
                               vm.getExcutingTask(),
                               vm));
        }

        if (vm.isFree()) {
            addEvent(new Event(currentTime + 0.5, Event.Type.VM_CHECK, vm));
        }
        task.updateSuccessor();
        addEvent(new Event(currentTime, Event.Type.SCHEDULE, workflow));
    }

    private void processWorkflowArrive(Workflow workflow) {
        workflow.addToReadyTaskList();
        addEvent(new Event(workflow.getArrivalTime(), Event.Type.SCHEDULE, workflow));
        for (Workflow.Task task : workflow.getTaskList()) {
          //  System.out.println("task latest start time:" + task.getLatestStartTime());
          // System.out.println("task excution time:" + task.getBaseExcutionTime());
            addEvent(new Event(task.getLatestStartTime(), Event.Type.TASK_ACHIEVE_PLST, task));
        }
    }

    private Event consumeEventFromPriorityQueue() {
        Event topEvent = priorityQueue.poll();
        if (topEvent.type == Event.Type.SCHEDULE) {
            while (topEvent.type == Event.Type.SCHEDULE && !priorityQueue.isEmpty() && priorityQueue.peek().type == Event.Type.SCHEDULE &&
            topEvent.timeStamp == priorityQueue.peek().timeStamp) {
                topEvent = priorityQueue.poll();
            }
        }
//        else if (topEvent.type == Event.Type.VM_CHECK) {
//            while (topEvent.type == Event.Type.VM_CHECK && !priorityQueue.isEmpty() && priorityQueue.peek().type == Event.Type.VM_CHECK &&
//                    topEvent.timeStamp == priorityQueue.peek().timeStamp) {
//                topEvent = priorityQueue.poll();
//            }
//        }

        return topEvent;
    }

}
