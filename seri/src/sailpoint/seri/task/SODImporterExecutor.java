package sailpoint.seri.task;

import sailpoint.task.AbstractTaskExecutor;

public abstract class SODImporterExecutor extends AbstractTaskExecutor {

	public static final String ROLES_CREATED = "rolesCreated";
	public static final String ROLES_SKIPPED = "rolesSkipped";
	public static final String SODS_CREATED = "sodsCreated";
	public static final String SODS_UPDATED = "sodsUpdated";

	protected static final String ownerName="spadmin";

	
}
