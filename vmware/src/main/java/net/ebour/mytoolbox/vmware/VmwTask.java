package net.ebour.mytoolbox.vmware;

import com.vmware.vim25.VirtualMachineCloneSpec;
import com.vmware.vim25.VirtualMachineRelocateSpec;
import com.vmware.vim25.mo.*;

abstract class VmwTask
{
    private static final String EXCEPTION = "exception";
    private static final String ERROR = "error";

    protected VirtualMachine vm;

    public void waitForTask() throws Exception {
        Task task = null;
        try {
            task = executeTask();
        } catch (Exception e) {
            throw new Exception(getMessage(EXCEPTION), e);
        }

        String status = waitForStatus(task);
        if (status != Task.SUCCESS) {
            throw new Exception(getMessage(ERROR));
        }
    }

    protected abstract Task executeTask() throws Exception;
    protected abstract String getMessage(String type);

    private String waitForStatus(Task task) throws Exception {
        String status = null;
        while (status == null) {
            try {
                status = task.waitForTask();
            } catch (InterruptedException ignore) {
                continue;
            } catch (Exception e) {
                final String msg = "VMWare exception waiting for task status";
                throw new Exception(msg, e);
            }
        }
        return status;
    }

    static final class CreateVMTask extends VmwTask {
        private static final String MSG_FMT =  "VMWare %s creating VM '%s' from template '%s'";

        private Folder folder;
        private String template;
        private String vmName;
        private VirtualMachineCloneSpec cloneSpec;

        CreateVMTask(VirtualMachine vm, ResourcePool pool, Folder folder, String template, String vmName) {
            this.vm = vm;
            this.folder = folder;
            this.template = template;
            this.vmName = vmName;

            VirtualMachineRelocateSpec relocateSpec = new VirtualMachineRelocateSpec();
            relocateSpec.setPool(pool.getMOR());

            cloneSpec = new VirtualMachineCloneSpec();
            cloneSpec.setLocation(relocateSpec);
            cloneSpec.setPowerOn(false);
            cloneSpec.setTemplate(false);
        }

        @Override
        protected Task executeTask() throws Exception {
            return vm.cloneVM_Task(folder, vmName, cloneSpec);
        }

        @Override
        protected String getMessage(String type) {
            return String.format(MSG_FMT, type, vmName, template);
        }
    }

    static final class PowerOnVMTask extends VmwTask {
        private static final String MSG_FMT = "VMWare %s powering on VM '%s'";

        private String vmName;

        PowerOnVMTask(VirtualMachine vm, String vmName) {
            this.vm = vm;
            this.vmName = vmName;
        }

        @Override
        protected Task executeTask() throws Exception {
            return vm.powerOnVM_Task(null);
        }

        @Override
        protected String getMessage(String type) {
            return String.format(MSG_FMT, type, vmName);
        }
    }

    static final class SnapshotVMTask extends VmwTask {
        private static final String MSG_FMT = "VMWare %s snapshotting VM '%s'";

        private String vmName;
        private String snapshotName;
        private String snapshotDesc;

        SnapshotVMTask(VirtualMachine vm, String vmName, String snapshotName, String snapshotDesc) {
            this.vm = vm;
            this.vmName = vmName;
            this.snapshotName = snapshotName;
            this.snapshotDesc = snapshotDesc;
        }

        @Override
        protected Task executeTask() throws Exception {
            return vm.createSnapshot_Task(snapshotName, snapshotDesc, false, true);
        }

        @Override
        protected String getMessage(String type) {
            return String.format(MSG_FMT, type, vmName);
        }
    }

    static final class RevertVMTask extends VmwTask {
        private static final String MSG_FMT = "VMWare %s reverting snapshot '%s'";

        private String snapshotName;
        private VirtualMachineSnapshot snapshot;

        RevertVMTask(VirtualMachine vm, String snapshotName, VirtualMachineSnapshot snapshot) {
            this.vm = vm;
            this.snapshotName = snapshotName;
            this.snapshot = snapshot;
        }

        @Override
        protected Task executeTask() throws Exception {
            if (snapshot == null) {
                return vm.revertToCurrentSnapshot_Task(null);
            } else {
                return snapshot.revertToSnapshot_Task(null);
            }
        }

        @Override
        protected String getMessage(String type) {
            return String.format(MSG_FMT, type, snapshotName);
        }
    }

    static final class PowerOffVMTask extends VmwTask {
        private static final String MSG_FMT = "VMWare %s powering off VM '%s'";

        private String vmName;

        PowerOffVMTask(VirtualMachine vm, String vmName) {
            this.vm = vm;
            this.vmName = vmName;
        }

        @Override
        protected Task executeTask() throws Exception {
            return vm.powerOffVM_Task();
        }

        @Override
        protected String getMessage(String type) {
            return String.format(MSG_FMT, type, vmName);
        }
    }

    static final class DestroyVMTask extends VmwTask {
        private static final String MSG_FMT = "VMWare %s destroying off VM '%s'";

        private String vmName;

        DestroyVMTask(VirtualMachine vm, String vmName) {
            this.vm = vm;
            this.vmName = vmName;
        }

        @Override
        protected Task executeTask() throws Exception {
            return vm.destroy_Task();
        }

        @Override
        protected String getMessage(String type) {
            return String.format(MSG_FMT, type, vmName);
        }
    }
}
