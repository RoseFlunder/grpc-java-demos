/*
 * Copyright 2016 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.grpc.springboot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.autoconfigure.grpc.client.DiscoveryClientResolverFactory;
import org.springframework.boot.autoconfigure.grpc.client.GrpcChannelFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.stereotype.Component;

import com.example.echo.EchoOuterClass;
import com.example.echo.EchoServiceGrpc;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.MethodDescriptor;
import io.grpc.util.RoundRobinLoadBalancerFactory;

/**
 * Created by rayt on 5/18/16.
 */
@Component
@EnableDiscoveryClient
public class Cmd {
	
	@Autowired
	public Cmd(ApplicationArguments args, GrpcChannelFactory channelFactory, DiscoveryClient client) {
		System.out.println("hello");

		//if client.getIstances is not called before here, the DiscoveryClientNameResolver will stop working when it
		//tries to call client.getInstances himself
		//why does it work when i call it here before once?
		for (ServiceInstance serviceInstance : client.getInstances("EchoService")) {
			System.out.println(serviceInstance.getServiceId() + ":" + serviceInstance.getPort());
		}
		
		
		//Using manahged channel builder directly instead of channel factory from auto configurer just to
		//be sure that the bug above has nothing to do with other spring magic
		ManagedChannel channel = ManagedChannelBuilder.forTarget("EchoService")
				.nameResolverFactory(new DiscoveryClientResolverFactory(client))
				.loadBalancerFactory(RoundRobinLoadBalancerFactory.getInstance())
				.usePlaintext(true)
				.build();
		
		EchoServiceGrpc.EchoServiceBlockingStub stub = EchoServiceGrpc.newBlockingStub(channel);
		
		int i = 0;
		while (true) {
			try {
				EchoOuterClass.Echo response = stub.echo(EchoOuterClass.Echo.newBuilder().setMessage("Hello " + i).build());
				System.out.println(response);
				i++;

				try {
					Thread.sleep(100L);
				} catch (InterruptedException e) {
				}
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
			
		}
	}
}
