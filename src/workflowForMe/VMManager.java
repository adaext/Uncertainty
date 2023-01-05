package workflowForMe;

import java.util.*;

/**
 * Provide active Vm List
 */
public class VMManager {
    List<VM> vmList = new ArrayList<>();
    List<VM> deActiveVmList = new ArrayList<>();
    public static Map<Integer, VM> vmMap = new HashMap<>();

    public List<VM> getActiveList() {
        List<VM> activeVmList = new ArrayList<>();
        for (VM vm : vmList) {
            if (vm.isActive()) {
                activeVmList.add(vm);
            }
        }
        return activeVmList;
    }

    public void checkAndRemoveVm(VM toBeDeleteVm, double currentTime) {
        if (toBeDeleteVm.isActive() && toBeDeleteVm.isFree()) {
            toBeDeleteVm.setDeactive(currentTime);
            deActiveVmList.add(toBeDeleteVm);
         //   System.out.println("Delete vm:" + toBeDeleteVm.getId());
        }
    }

    public void addNewActiveVM(VM vm) {
        assert vm.isActive();
        vmMap.put(vm.getId(), vm);
      //  System.out.println("Add vm:" + vm.getId());
        vmList.add(vm);
    }

    public double getTotalCost(){
        System.out.println("vm: " + vmList.size() + " " + deActiveVmList.size());
        for (VM vm :vmList) {
            System.out.println(vm.getId() + " is active:" + vm.isActive() + " is free:" + vm.isFree());
        }
        assert vmList.size() == deActiveVmList.size():"vms not all killed";
        double totalCost = 0;
        for(VM vm:deActiveVmList) {
            totalCost += vm.getEndTime() - vm.getStartTime();
        }
        return totalCost;
    }
}
