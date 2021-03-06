// Copyright (c) 2003-present, Jodd Team (jodd.org). All Rights Reserved.

package jodd.jtx;

import jodd.util.ReflectUtil;

/**
 * Stores resource object and its resource manager.
 */
final class JtxResource<E> {

	final JtxTransaction transaction;
	final JtxResourceManager<E> resourceManager;
	private final E resource;

	JtxResource(JtxTransaction transaction, JtxResourceManager<E> resourceManager, E resource) {
		this.transaction = transaction;
		this.resourceManager = resourceManager;
		this.resource = resource;
	}

	/**
	 * Returns <code>true</code> if resource is of provided resource type.
	 */
	public boolean isSameTypeAsResource(Class type) {
		return ReflectUtil.isTypeOf(type, resource.getClass());
	}

	// ---------------------------------------------------------------- delegates

	/**
	 * Delegates to {@link jodd.jtx.JtxResourceManager#commitTransaction(Object)}.
	 */
	void commitTransaction() {
		resourceManager.commitTransaction(resource);
	}

	/**
	 * Delegates to {@link JtxResourceManager#rollbackTransaction(Object)}}.
	 */
	void rollbackTransaction() {
		resourceManager.rollbackTransaction(resource);
	}

	/**
	 * Returns resource instance.
s	 */
	public E getResource() {
		return resource;
	}
}