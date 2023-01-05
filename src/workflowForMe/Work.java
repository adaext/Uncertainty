package workflowForMe;

import share.StaticfinalTags;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

public class Work {
    private static List<Workflow> workflows; //The set of workflows

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        // Obtain the set of workflows

        workflows = getWorkflowsFromFile("E:\\Uncertainty\\MyProducedWorkflow.txt", StaticfinalTags.workflowNum);
        System.out.println("workflows size: " + workflows.size());
         EventManager eventManager = new EventManager();
        for (Workflow workflow: workflows) {
            Event event = new Event(workflow.getArrivalTime(), Event.Type.WORKFLOW_ARRIVE, workflow);
            eventManager.addEvent(event);
        }

        while (eventManager.hasEvent()) {
            eventManager.consumeEvent();
        }
        for (Workflow workflow : workflows) {
            for(Workflow.Task task : workflow.getTaskList()) {
                System.out.println(task.getMessage());
            }
        }
        System.out.println("Total cost:" + eventManager.vmManager.getTotalCost());
    }


    /**Obtain the set of workflows
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static List<Workflow> getWorkflowsFromFile(String filename, int workflowNum) throws IOException, ClassNotFoundException {
        List<Workflow> w_List = new ArrayList<Workflow>();
        Workflow w = null;
        FileInputStream fi = new FileInputStream(filename);
        ObjectInputStream si = new ObjectInputStream(fi);
        try
        {
            int minTasksNum = Integer.MAX_VALUE;
            Workflow minWorkflow = new Workflow((workflow.Workflow) si.readObject());
            for(int i=1; i<workflowNum; i++)
            {
                w = new Workflow((workflow.Workflow) si.readObject());
                if (w.getSize() < minTasksNum) {
                    minWorkflow = w;
                }
            }
            w_List.add(minWorkflow);
            si.close();
        }catch(IOException e){System.out.println(e.getMessage());}
        return w_List;
    }
}
