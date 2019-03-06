package com.redhat.usecase.service;

import javax.ws.rs.core.Response;
import com.customer.app.Person;

public interface DEIMService {
  public Response addPerson(Person person);
  
  public Response updatePerson(Person person);
  
  public Response searchPerson(String name);
}
