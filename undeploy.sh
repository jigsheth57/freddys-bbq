#!/bin/bash

echo y| cf d -r admin-portal
echo y| cf d -r customer-portal
echo y| cf d -r order-service
echo y| cf d -r menu-service
echo y| cf d -r zipkin
