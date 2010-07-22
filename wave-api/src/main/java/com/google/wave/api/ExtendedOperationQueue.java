package com.google.wave.api;

import java.util.List;

import com.google.wave.api.JsonRpcConstant.ParamsProperty;
import com.google.wave.api.OperationRequest.Parameter;

public class ExtendedOperationQueue extends OperationQueue {
	
	public ExtendedOperationQueue() {
		super();
	}
	
	public ExtendedOperationQueue(String proxyForId) {
		super(proxyForId);
	}
	
	public ExtendedOperationQueue(List<OperationRequest> operations, String proxyForId) {
		super(operations, proxyForId);
	}
	
	
	OperationRequest search(String query) {
		return super.appendOperation(OperationType.ROBOT_SEARCH, Parameter.of(ParamsProperty.QUERY, query));
	}
}