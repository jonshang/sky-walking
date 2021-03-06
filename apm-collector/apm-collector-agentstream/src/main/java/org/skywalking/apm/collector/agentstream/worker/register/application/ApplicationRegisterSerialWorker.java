package org.skywalking.apm.collector.agentstream.worker.register.application;

import org.skywalking.apm.collector.agentstream.worker.register.IdAutoIncrement;
import org.skywalking.apm.collector.agentstream.worker.register.application.dao.IApplicationDAO;
import org.skywalking.apm.collector.storage.dao.DAOContainer;
import org.skywalking.apm.collector.stream.worker.AbstractLocalAsyncWorker;
import org.skywalking.apm.collector.stream.worker.AbstractLocalAsyncWorkerProvider;
import org.skywalking.apm.collector.stream.worker.ClusterWorkerContext;
import org.skywalking.apm.collector.stream.worker.ProviderNotFoundException;
import org.skywalking.apm.collector.stream.worker.Role;
import org.skywalking.apm.collector.stream.worker.WorkerException;
import org.skywalking.apm.collector.stream.worker.impl.data.DataDefine;
import org.skywalking.apm.collector.stream.worker.selector.ForeverFirstSelector;
import org.skywalking.apm.collector.stream.worker.selector.WorkerSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author pengys5
 */
public class ApplicationRegisterSerialWorker extends AbstractLocalAsyncWorker {

    private final Logger logger = LoggerFactory.getLogger(ApplicationRegisterSerialWorker.class);

    public ApplicationRegisterSerialWorker(Role role, ClusterWorkerContext clusterContext) {
        super(role, clusterContext);
    }

    @Override public void preStart() throws ProviderNotFoundException {
        super.preStart();
    }

    @Override protected void onWork(Object message) throws WorkerException {
        if (message instanceof ApplicationDataDefine.Application) {
            ApplicationDataDefine.Application application = (ApplicationDataDefine.Application)message;
            logger.debug("register application, application code: {}", application.getApplicationCode());

            IApplicationDAO dao = (IApplicationDAO)DAOContainer.INSTANCE.get(IApplicationDAO.class.getName());
            int min = dao.getMinApplicationId();
            if (min == 0) {
                application.setApplicationId(1);
                application.setId("1");
            } else {
                int max = dao.getMaxApplicationId();
                int applicationId = IdAutoIncrement.INSTANCE.increment(min, max);
                application.setApplicationId(applicationId);
                application.setId(String.valueOf(applicationId));
            }
            dao.save(application);
        }
    }

    public static class Factory extends AbstractLocalAsyncWorkerProvider<ApplicationRegisterSerialWorker> {
        @Override
        public Role role() {
            return WorkerRole.INSTANCE;
        }

        @Override
        public ApplicationRegisterSerialWorker workerInstance(ClusterWorkerContext clusterContext) {
            return new ApplicationRegisterSerialWorker(role(), clusterContext);
        }

        @Override public int queueSize() {
            return 256;
        }
    }

    public enum WorkerRole implements Role {
        INSTANCE;

        @Override
        public String roleName() {
            return ApplicationRegisterSerialWorker.class.getSimpleName();
        }

        @Override
        public WorkerSelector workerSelector() {
            return new ForeverFirstSelector();
        }

        @Override public DataDefine dataDefine() {
            return new ApplicationDataDefine();
        }
    }
}
