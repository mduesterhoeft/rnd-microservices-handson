package com.epages.microservice.handson.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/orders")
@ExposesResourceFor(Order.class)
public class OrderController {

    private OrderService orderService;
    private EntityLinks entityLinks;
    private OrderResourceAssembler orderResourceAssembler;

    @Autowired
    public OrderController(OrderService orderService,
                           EntityLinks entityLinks,
                           OrderResourceAssembler orderResourceAssembler) {

        this.orderService = orderService;
        this.entityLinks = entityLinks;
        this.orderResourceAssembler = orderResourceAssembler;
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<OrderResource> get(@PathVariable Long id) {
        return orderService.getOrder(id)
                .map(orderResourceAssembler::toResource)
                .map(ResponseEntity::ok)
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }

    @RequestMapping(method = GET)
    public ResponseEntity<PagedResources<OrderResource>> getAll(Pageable pageable, PagedResourcesAssembler<Order> pagedResourcesAssembler) {
        final Page<Order> orders = orderService.getAll(pageable);
        final PagedResources<OrderResource> body = pagedResourcesAssembler.toResource(orders, orderResourceAssembler);
        return ResponseEntity.ok(body);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Void> create(@RequestBody OrderCreationResource orderResource) {

        Order order = new Order();
        order.setDeliveryAddress(orderResource.getDeliveryAddress());
        order.setComment(orderResource.getComment());
        order.setItems(orderResource.getItems().stream()
                .map(LineItemResource::toEntity)
                .collect(Collectors.toList()));
        order = orderService.create(order);
        URI location = entityLinks.linkForSingleResource(Order.class, order.getId()).toUri();
        return ResponseEntity.created(location).build();

    }

}