package com.redhat.translate.test;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.activemq.broker.Broker;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.AssertionClause;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringTestSupport;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TranslateTest extends CamelSpringTestSupport{
	
	@Produce(uri = "activemqService:queue:translatetest")
	protected ProducerTemplate inputEndpoint;
	
	@Test
	public void testConverterRoute() throws Exception{
		MockEndpoint mockEndpoint = getMockEndpoint("mock:translateEndpoint");
		inputEndpoint.sendBody("activemqService:queue:translatetest",getDataFromFile());
		AssertionClause expectedBodyReceived = mockEndpoint.expectedBodyReceived();
		expectedBodyReceived.body().isInstanceOf(com.sun.mdm.index.webservice.ExecuteMatchUpdate.class);
		mockEndpoint.assertIsSatisfied();
	}
	
	@Override
	protected ClassPathXmlApplicationContext createApplicationContext() {
		return new ClassPathXmlApplicationContext("camelTestContext.xml");
	}
	
	@SuppressWarnings("resource")
	public String getDataFromFile() {
		String finalLine = "";
		try {
			FileReader reader = new FileReader("src/test/data/Person.xml");
			BufferedReader bufferedReader = new BufferedReader(reader);
			String currentLine = null;

			while ((currentLine = bufferedReader.readLine()) != null) {
				finalLine += currentLine;
			}
		} catch (IOException io) {
			System.out.println(io.getMessage());
		}
		return finalLine;
	}

}
