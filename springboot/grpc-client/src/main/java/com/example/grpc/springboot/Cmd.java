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

import java.util.concurrent.CountDownLatch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.grpc.client.GrpcChannelFactory;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.stereotype.Component;

import com.example.echo.EchoOuterClass.Echo;
import com.example.echo.EchoServiceGrpc;
import com.example.echo.EchoServiceGrpc.EchoServiceStub;

import io.grpc.Channel;
import io.grpc.stub.StreamObserver;

/**
 * Created by rayt on 5/18/16.
 */
@Component
@EnableDiscoveryClient
public class Cmd implements CommandLineRunner {
	
	private final GrpcChannelFactory channelFactory;

	@Autowired
	public Cmd(ApplicationArguments args, GrpcChannelFactory channelFactory) {
		this.channelFactory = channelFactory;
	}

	@Override
	public void run(String... args) throws Exception {		
		Channel channel = channelFactory.createChannel("EchoService");
//		EchoServiceGrpc.EchoServiceBlockingStub stub = EchoServiceGrpc.newBlockingStub(channel);
//		
//		int i = 0;
//		while (true) {
//			try {
//				EchoOuterClass.Echo response = stub.echo(EchoOuterClass.Echo.newBuilder().setMessage("Hello " + i).build());
//				System.out.println(response);
//				i++;
//
//				try {
//					Thread.sleep(2000L);
//				} catch (InterruptedException e) {
//				}
//			} catch (Exception e) {
//				System.err.println(e.getMessage());
//			}
//		}
		
		CountDownLatch latch = new CountDownLatch(1);
		EchoServiceStub stub = EchoServiceGrpc.newStub(channel);
		stub.echo(Echo.newBuilder().setMessage("Hello").build(), new StreamObserver<Echo>() {

			@Override
			public void onNext(Echo value) {
				System.out.println(value.getMessage());
			}

			@Override
			public void onError(Throwable t) {
				latch.countDown();
				
			}

			@Override
			public void onCompleted() {
				System.out.println("Finished");
				latch.countDown();
			}
		});
		
		latch.await();
	}
}
