const docsV1 =
  {
    'openapi': '3.0.3',
    'info': {
      'title': 'Oval API documentation- OpenAPI 3.0',
      'description': 'The official documentation of the endpoints implemented in our mobil server',
      'contact': {
        'email': 'alvin@lattis.io'
      },
      'license': {
        'name': 'Apache 2.0',
        'url': 'http://www.apache.org/licenses/LICENSE-2.0.html'
      },
      'version': '1.0.11'
    },
    'externalDocs': {
      'description': 'Find out more about Swagger',
      'url': 'http://swagger.io'
    },
    'servers': [
      {
        'url': 'https://lattisappv2.lattisapi.io/v2/api/'
      }
    ],
    'tags': [
      {
        'name': 'Trips',
        'description': 'The logic behind trips in our system',
        'externalDocs': {
          'description': 'Find out more',
          'url': 'http://swagger.io'
        }
      }
    ],
    'paths': {
      '/trips/start-trip': {
        'post': {
          'tags': [
            'Trips'
          ],
          'summary': 'Start a trip',
          'description': 'Start a trip',
          'operationId': 'startTrip',
          'requestBody': {
            'description': 'Start a trip on mobile',
            'content': {
              'application/json': {
                'schema': {
                  '$ref': '#/components/schemas/startTrip'
                }
              }
            },
            'required': true
          },
          'responses': {
            '200': {
              'description': 'Successful operation',
              'content': {
                'application/json': {
                  'schema': {
                    '$ref': '#/components/schemas/startTripResponse'
                  }
                }
              }
            },
            '412': {
              'description': 'Invalid token'
            },
            '404': {
              'description': 'Resource not found'
            },
            '500': {
              'description': 'Internal server error'
            }
          },
          'security': [
            {
              'accessToken': [
              ]
            }
          ]
        }
      },
      '/trips/update-trip': {
        'post': {
          'tags': [
            'Trips'
          ],
          'summary': 'Update a trip',
          'description': 'Update a trip',
          'operationId': 'updateTrip',
          'requestBody': {
            'description': 'Update a trip on mobile',
            'content': {
              'application/json': {
                'schema': {
                  '$ref': '#/components/schemas/updateTrip'
                }
              }
            },
            'required': true
          },
          'responses': {
            '200': {
              'description': 'Successful operation',
              'content': {
                'application/json': {
                  'schema': {
                    '$ref': '#/components/schemas/updateTripResponse'
                  }
                }
              }
            },
            '412': {
              'description': 'Invalid token'
            },
            '404': {
              'description': 'Resource not found'
            },
            '500': {
              'description': 'Internal server error'
            }
          },
          'security': [
            {
              'accessToken': [
              ]
            }
          ]
        }
      },
      '/trips/get-trip-details': {
        'post': {
          'tags': [
            'Trips'
          ],
          'summary': 'Get a trip by trip id',
          'description': 'Get a trip by trip id',
          'operationId': 'getTrip',
          'requestBody': {
            'description': 'Get a trip from the database',
            'content': {
              'application/json': {
                'schema': {
                  '$ref': '#/components/schemas/Trip'
                }
              }
            },
            'required': true
          },
          'responses': {
            '200': {
              'description': 'Successful operation',
              'content': {
                'application/json': {
                  'schema': {
                    '$ref': '#/components/schemas/startTrip'
                  }
                }
              }
            },
            '412': {
              'description': 'Invalid token'
            },
            '500': {
              'description': 'Internal server error'
            }
          },
          'security': [
            {
              'accessToken': [
              ]
            }
          ]
        }
      },
      '/trips/end-trip': {
        'post': {
          'tags': [
            'Trips'
          ],
          'summary': 'End a trip',
          'description': 'End a trip',
          'operationId': 'endTrip',
          'requestBody': {
            'description': 'End a trip on mobile',
            'content': {
              'application/json': {
                'schema': {
                  '$ref': '#/components/schemas/endTrip'
                }
              }
            },
            'required': true
          },
          'responses': {
            '200': {
              'description': 'Successful operation',
              'content': {
                'application/json': {
                  'schema': {
                    '$ref': '#/components/schemas/endTripResponse'
                  }
                }
              }
            },
            '412': {
              'description': 'Invalid token'
            },
            '404': {
              'description': 'Resource not found'
            },
            '500': {
              'description': 'Internal server error'
            }
          },
          'security': [
            {
              'accessToken': [
              ]
            }
          ]
        }
      },
      '/trips/update-rating': {
        'post': {
          'tags': [
            'Trips'
          ],
          'summary': 'Give a trip rating',
          'description': 'Give a trip rating',
          'operationId': 'rateTrip',
          'requestBody': {
            'description': 'Rate a trip on mobile',
            'content': {
              'application/json': {
                'schema': {
                  '$ref': '#/components/schemas/rateTrip'
                }
              }
            },
            'required': true
          },
          'responses': {
            '200': {
              'description': 'Successful operation',
              'content': {
                'application/json': {
                  'schema': {
                    '$ref': '#/components/schemas/rateTripResponse'
                  }
                }
              }
            },
            '412': {
              'description': 'Invalid token'
            },
            '404': {
              'description': 'Resource not found'
            },
            '500': {
              'description': 'Internal server error'
            }
          },
          'security': [
            {
              'accessToken': [
              ]
            }
          ]
        }
      }
    },
    'components': {
      'schemas': {
        'Trip': {
          'required': [
            'trip_id'
          ],
          'type': 'object',
          'properties': {
            'trip_id': {
              'type': 'integer',
              'format': 'int64',
              'example': 9588
            }
          }
        },
        'updateTrip': {
          'required': [
            'trip_id',
            'steps'
          ],
          'type': 'object',
          'properties': {
            'trip_id': {
              'type': 'integer',
              'format': 'int64',
              'example': 9588
            },
            'steps': {
              'type': 'array',
              'example': [['-1.5188753', '37.2660673', 1659984191]]
            }
          }
        },
        'TripResponse': {
          'type': 'object',
          'properties': {
            'trip': {
              'type': 'object',
              'example': '{\n' +
                '\t\t\t"trip_id": 9588,\n' +
                '\t\t\t"steps": [\n' +
                '\t\t\t\t[]\n' +
                '\t\t\t],\n' +
                '\t\t\t"start_address": null,\n' +
                '\t\t\t"end_address": null,\n' +
                '\t\t\t"date_created": null,\n' +
                '\t\t\t"rating": null,\n' +
                '\t\t\t"date_endtrip": null,\n' +
                '\t\t\t"parking_image": null,\n' +
                '\t\t\t"user_id": 969,\n' +
                '\t\t\t"operator_id": 22,\n' +
                '\t\t\t"customer_id": 4,\n' +
                '\t\t\t"bike_id": 964,\n' +
                '\t\t\t"first_lock_connect": false,\n' +
                '\t\t\t"fleet_id": 23,\n' +
                '\t\t\t"reservation_id": 460,\n' +
                '\t\t\t"vendor": "Duckt",\n' +
                '\t\t\t"pricing_option_id": 70,\n' +
                '\t\t\t"port_id": null,\n' +
                '\t\t\t"hub_id": null,\n' +
                '\t\t\t"device_type": "bike",\n' +
                '\t\t\t"reservation_start": "2022-07-09T14:00:00.000Z",\n' +
                '\t\t\t"reservation_end": "2022-07-09T15:00:00.000Z",\n' +
                '\t\t\t"reservation_terminated": null,\n' +
                '\t\t\t"termination_reason": null,\n' +
                '\t\t\t"duration": 1659973417,\n' +
                '\t\t\t"distance": 0,\n' +
                '\t\t\t"fleet_name": "Velo Labs",\n' +
                '\t\t\t"key": "f77825d6c77ebf312afae25522b0903e",\n' +
                '\t\t\t"type": "private",\n' +
                '\t\t\t"logo": "https://s3-us-west-1.amazonaws.com/lattis.bikes.customers/velo%20labs%20logo.png",\n' +
                '\t\t\t"t_and_c": "https://s3-us-west-1.amazonaws.com/lattis.production/fleet_t_and_c/terms_conditions_giraff_2021.html",\n' +
                '\t\t\t"skip_parking_image": true,\n' +
                '\t\t\t"max_trip_length": null,\n' +
                '\t\t\t"contact_first_name": "Jeremy",\n' +
                '\t\t\t"contact_last_name": "Ricard",\n' +
                '\t\t\t"contact_email": "jeremy@lattis.io",\n' +
                '\t\t\t"contact_phone": "+14154202326",\n' +
                '\t\t\t"contract_file": null,\n' +
                '\t\t\t"address_id": null,\n' +
                '\t\t\t"country_code": "null",\n' +
                '\t\t\t"parking_area_restriction": 0,\n' +
                '\t\t\t"member_csv": "https://s3-us-west-1.amazonaws.com/lattis.development%2Fmember_lists/member_csv-23yLoo1tMy0s",\n' +
                '\t\t\t"price_for_penalty_outside_spot": 0,\n' +
                '\t\t\t"parking_spot_restriction": 0,\n' +
                '\t\t\t"distance_preference": "kilometers",\n' +
                '\t\t\t"start_trip_email": "velo_labs.txt",\n' +
                '\t\t\t"fleet_membership_id": 4,\n' +
                '\t\t\t"contact_web_link": null,\n' +
                '\t\t\t"company_id": null,\n' +
                '\t\t\t"id": 439,\n' +
                '\t\t\t"price_for_active_bike": 0,\n' +
                '\t\t\t"price_for_staging_bike": 0,\n' +
                '\t\t\t"price_for_outofservice_bike": 0,\n' +
                '\t\t\t"price_for_archived_bike": 0,\n' +
                '\t\t\t"payment_mode": "Offline",\n' +
                '\t\t\t"payment_period": "Annual",\n' +
                '\t\t\t"next_billing_date": null,\n' +
                '\t\t\t"current_plan_name": "",\n' +
                '\t\t\t"account_status": "Active",\n' +
                '\t\t\t"stripe_account_id": "acct_1BBnemAxq1K8VJvJ",\n' +
                '\t\t\t"price_for_membership": 0,\n' +
                '\t\t\t"price_type_value": 1,\n' +
                '\t\t\t"price_type": "Mins",\n' +
                '\t\t\t"usage_surcharge": "No",\n' +
                '\t\t\t"excess_usage_fees": 0,\n' +
                '\t\t\t"excess_usage_type_value": 0,\n' +
                '\t\t\t"excess_usage_type": "",\n' +
                '\t\t\t"excess_usage_type_after_value": 0,\n' +
                '\t\t\t"excess_usage_type_after_type": "",\n' +
                '\t\t\t"price_for_penalty_outside_parking": 0,\n' +
                '\t\t\t"price_for_penalty_outside_parking_below_battery_charge": 0,\n' +
                '\t\t\t"price_for_forget_plugin": 0,\n' +
                '\t\t\t"ride_deposit": "No",\n' +
                '\t\t\t"price_for_ride_deposit": 0,\n' +
                '\t\t\t"price_for_ride_deposit_type": "OneTime",\n' +
                '\t\t\t"refund_criteria": 0,\n' +
                '\t\t\t"refund_criteria_value": "Days",\n' +
                '\t\t\t"price_for_penalty_outside_zone": 0,\n' +
                '\t\t\t"currency": "USD",\n' +
                '\t\t\t"price_for_bike_unlock": 1,\n' +
                '\t\t\t"price_for_reservation_late_return": 0,\n' +
                '\t\t\t"payment_gateway": "stripe",\n' +
                '\t\t\t"enable_preauth": false,\n' +
                '\t\t\t"preauth_amount": 0,\n' +
                '\t\t\t"transaction_id": "pi_3LJf41Axq1K8VJvJ1cjVqLWy",\n' +
                '\t\t\t"charge_for_duration": 20,\n' +
                '\t\t\t"penalty_fees": 0,\n' +
                '\t\t\t"deposit": 0,\n' +
                '\t\t\t"total": 20,\n' +
                '\t\t\t"over_usage_fees": 0,\n' +
                '\t\t\t"user_profile_id": "cus_GKszNSAtlGuW18",\n' +
                '\t\t\t"card_id": "pm_1LJdxbFvbisHamMm6C9k5E8X",\n' +
                '\t\t\t"date_charged": 1657378522,\n' +
                '\t\t\t"application_fee": null,\n' +
                '\t\t\t"bike_unlock_fee": 0,\n' +
                '\t\t\t"membership_discount": 0,\n' +
                '\t\t\t"membership_subscription_id": null,\n' +
                '\t\t\t"promo_code_discount": null,\n' +
                '\t\t\t"promotion_id": null,\n' +
                '\t\t\t"status": "succeeded",\n' +
                '\t\t\t"gateway_status": "succeeded",\n' +
                '\t\t\t"amount_captured": 20,\n' +
                '\t\t\t"taxes": null,\n' +
                '\t\t\t"tax_sub_total": 0,\n' +
                '\t\t\t"type_card": "Corporate",\n' +
                '\t\t\t"cc_no": "xxxxxxxxxxxx4444",\n' +
                '\t\t\t"cc_type": "MasterCard",\n' +
                '\t\t\t"payment_method": "pm_1LJdxbFvbisHamMm6C9k5E8X",\n' +
                '\t\t\t"first_six_digits": null,\n' +
                '\t\t\t"last_four_digits": null,\n' +
                '\t\t\t"source_fleet_id": 1,\n' +
                '\t\t\t"pricing_option": {\n' +
                '\t\t\t\t"pricing_option_id": 70,\n' +
                '\t\t\t\t"fleet_id": 23,\n' +
                '\t\t\t\t"duration": 1,\n' +
                '\t\t\t\t"duration_unit": "days",\n' +
                '\t\t\t\t"grace_period": null,\n' +
                '\t\t\t\t"grace_period_unit": null,\n' +
                '\t\t\t\t"price": 20,\n' +
                '\t\t\t\t"price_currency": "USD",\n' +
                '\t\t\t\t"deactivated_at": "2022-07-13T11:12:23Z",\n' +
                '\t\t\t\t"deactivation_reason": "deactivation",\n' +
                '\t\t\t\t"created_at": "2022-07-05T18:47:11Z"\n' +
                '\t\t\t}\n' +
                '\t\t}'
            },
            'bike': {
              'type': 'object',
              'example': '{\n' +
                '\t\t\t"bike": {\n' +
                '\t\t\t\t"bike_id": 964,\n' +
                '\t\t\t\t"bike_name": "Duckt test",\n' +
                '\t\t\t\t"date_created": 1591913913,\n' +
                '\t\t\t\t"status": "active",\n' +
                '\t\t\t\t"bike_battery_level": null,\n' +
                '\t\t\t\t"current_status": "parked",\n' +
                '\t\t\t\t"maintenance_status": null,\n' +
                '\t\t\t\t"distance": 117158874.67374425,\n' +
                '\t\t\t\t"latitude": 45.494118,\n' +
                '\t\t\t\t"longitude": -73.611627,\n' +
                '\t\t\t\t"lock_id": null,\n' +
                '\t\t\t\t"fleet_id": 23,\n' +
                '\t\t\t\t"distance_after_service": 0,\n' +
                '\t\t\t\t"qr_code_id": 847561,\n' +
                '\t\t\t\t"parking_spot_id": null,\n' +
                '\t\t\t\t"bike_group_id": 103,\n' +
                '\t\t\t\t"bike_uuid": "3599deb6-68d0-46b6-9ab6-3837a26e1181",\n' +
                '\t\t\t\t"created_at": "2021-05-24T08:58:38.000Z",\n' +
                '\t\t\t\t"updated_at": "2021-05-24T08:58:38.000Z"\n' +
                '\t\t\t},\n' +
                '\t\t\t"fleet": {\n' +
                '\t\t\t\t"fleet_id": 23,\n' +
                '\t\t\t\t"fleet_name": "Velo Labs",\n' +
                '\t\t\t\t"date_created": 1495434550,\n' +
                '\t\t\t\t"customer_id": 4,\n' +
                '\t\t\t\t"operator_id": 22,\n' +
                '\t\t\t\t"key": "f77825d6c77ebf312afae25522b0903e",\n' +
                '\t\t\t\t"type": "private",\n' +
                '\t\t\t\t"logo": "https://s3-us-west-1.amazonaws.com/lattis.bikes.customers/velo%20labs%20logo.png",\n' +
                '\t\t\t\t"t_and_c": "https://s3-us-west-1.amazonaws.com/lattis.production/fleet_t_and_c/terms_conditions_giraff_2021.html",\n' +
                '\t\t\t\t"skip_parking_image": 1,\n' +
                '\t\t\t\t"max_trip_length": null,\n' +
                '\t\t\t\t"contact_first_name": "Jeremy",\n' +
                '\t\t\t\t"contact_last_name": "Ricard",\n' +
                '\t\t\t\t"contact_email": "jeremy@lattis.io",\n' +
                '\t\t\t\t"contact_phone": "+14154202326",\n' +
                '\t\t\t\t"contract_file": null,\n' +
                '\t\t\t\t"address_id": null,\n' +
                '\t\t\t\t"country_code": "null",\n' +
                '\t\t\t\t"parking_area_restriction": 0,\n' +
                '\t\t\t\t"member_csv": "https://s3-us-west-1.amazonaws.com/lattis.development%2Fmember_lists/member_csv-23yLoo1tMy0s",\n' +
                '\t\t\t\t"price_for_penalty_outside_spot": "0.00",\n' +
                '\t\t\t\t"parking_spot_restriction": 0,\n' +
                '\t\t\t\t"distance_preference": "kilometers",\n' +
                '\t\t\t\t"start_trip_email": "velo_labs.txt",\n' +
                '\t\t\t\t"fleet_membership_id": 4,\n' +
                '\t\t\t\t"contact_web_link": null,\n' +
                '\t\t\t\t"company_id": null\n' +
                '\t\t\t}\n' +
                '\t\t}'
            }
          }
        },
        'startTrip': {
          'required': [
            'latitude',
            'longitude',
            'bike_id'
          ],
          'type': 'object',
          'properties': {
            'latitude': {
              'type': 'string',
              'example': '-1.5188753'
            },
            'longitude': {
              'type': 'string',
              'example': '37.2660673'
            },
            'bike_id': {
              'type': 'integer',
              'example': 880
            }
          }
        },
        'endTrip': {
          'required': [
            'latitude',
            'longitude',
            'trip_id'
          ],
          'type': 'object',
          'properties': {
            'latitude': {
              'type': 'string',
              'example': '-1.5188753'
            },
            'longitude': {
              'type': 'string',
              'example': '37.2660673'
            },
            'trip_id': {
              'type': 'integer',
              'example': 9805
            }
          }
        },
        'rateTrip': {
          'required': [
            'trip_id',
            'rating'
          ],
          'type': 'object',
          'properties': {
            'trip_id': {
              'type': 'integer',
              'example': 9805
            },
            'rating': {
              'type': 'integer',
              'example': 5
            }
          }
        },
        'startTripResponse': {
          'type': 'object',
          'properties': {
            'trip_id': {
              'type': 'integer',
              'example': 9805
            },
            'do_not_track_trip': {
              'type': 'boolean',
              'example': true
            }
          }
        },
        'updateTripResponse': {
          'type': 'object',
          'properties': {
            'payload': {
              'type': 'object',
              'example': {
                'trip_id': 9805,
                'duration': 1329,
                'amount': 0,
                'membership_discount': 0,
                'promo_code_discount': 0,
                'charge_for_duration': 0,
                'currency': 'USD',
                'do_not_track_trip': true,
                'bike_id': 880,
                'bike_battery_level': null
              }
            }
          }
        },
        'endTripResponse': {
          'type': 'object',
          'properties': {
            'payload': {
              'type': 'object',
              'example': {
                'trip_id': 9805,
                'steps': [
                  [
                    '-1.5188753',
                    '37.2660673',
                    1659984191
                  ],
                  [
                    '-1.5188753',
                    '37.2660673',
                    1659988295
                  ]
                ],
                'start_address': 'Unnamed Road',
                'end_address': 'Unnamed Road -1.5188753,37.2660673',
                'date_created': 1659984188,
                'rating': null,
                'date_endtrip': 1659988295,
                'parking_image': null,
                'user_id': 969,
                'operator_id': 44,
                'customer_id': 4,
                'bike_id': 880,
                'first_lock_connect': true,
                'fleet_id': 82,
                'reservation_id': null,
                'vendor': null,
                'pricing_option_id': null,
                'port_id': null,
                'hub_id': null,
                'device_type': 'bike',
                'reservation_start': null,
                'reservation_end': null,
                'reservation_terminated': null,
                'termination_reason': null,
                'duration': 4107,
                'distance': 0,
                'fleet_name': 'baisKeli',
                'key': '8a988f71cbb422346c5d4b307d3dc860',
                'type': 'private_no_payment',
                'logo': 'https://s3-us-west-1.amazonaws.com/lattis.development%2Ffleet_logos/fleet_logo-baisKeli',
                't_and_c': 'https://s3.us-west-1.amazonaws.com/lattis.production/fleet_t_and_c/hooba_t_%26_c/cofinimo_july_2022.html',
                'skip_parking_image': true,
                'max_trip_length': null,
                'contact_first_name': 'Alvin',
                'contact_last_name': 'Mutisya',
                'contact_email': 'alvin@lattis.io',
                'contact_phone': '+254712003328',
                'contract_file': null,
                'address_id': null,
                'country_code': 'null',
                'parking_area_restriction': 0,
                'member_csv': null,
                'price_for_penalty_outside_spot': 0,
                'parking_spot_restriction': 0,
                'distance_preference': 'kilometers',
                'start_trip_email': null,
                'fleet_membership_id': 8,
                'contact_web_link': null,
                'company_id': null,
                'id': 83,
                'price_for_active_bike': 0,
                'price_for_staging_bike': 0,
                'price_for_outofservice_bike': 0,
                'price_for_archived_bike': 0,
                'payment_mode': 'Offline',
                'payment_period': 'Annual',
                'next_billing_date': null,
                'current_plan_name': '',
                'account_status': 'Active',
                'stripe_account_id': 'acct_1FqOehBFbVGXK4Bw',
                'price_for_membership': 0,
                'price_type_value': 2,
                'price_type': 'Mins',
                'usage_surcharge': 'No',
                'excess_usage_fees': 0,
                'excess_usage_type_value': 0,
                'excess_usage_type': '',
                'excess_usage_type_after_value': 0,
                'excess_usage_type_after_type': '',
                'price_for_penalty_outside_parking': 20,
                'price_for_penalty_outside_parking_below_battery_charge': 0,
                'price_for_forget_plugin': 0,
                'ride_deposit': 'No',
                'price_for_ride_deposit': 0,
                'price_for_ride_deposit_type': 'OneTime',
                'refund_criteria': 0,
                'refund_criteria_value': 'Weeks',
                'price_for_penalty_outside_zone': 0,
                'currency': 'USD',
                'price_for_bike_unlock': 0,
                'price_for_reservation_late_return': null,
                'payment_gateway': 'stripe',
                'enable_preauth': true,
                'preauth_amount': 100,
                'transaction_id': null,
                'charge_for_duration': 0,
                'penalty_fees': 0,
                'deposit': 0,
                'total': 0,
                'over_usage_fees': 0,
                'user_profile_id': null,
                'card_id': null,
                'date_charged': null,
                'type_card': null,
                'cc_no': null,
                'cc_type': null,
                'do_not_track_trip': false
              }
            }
          }
        },
        'rateTripResponse': {
          'type': 'object',
          'properties': {
            'response': {
              'type': 'object',
              'example': {
                'error': null,
                'payload': null,
                'status': 200
              }
            }
          }
        }
      },
      'requestBodies': {
        'Pet': {
          'description': 'Pet object that needs to be added to the store',
          'content': {
            'application/json': {
              'schema': {
                '$ref': '#/components/schemas/Pet'
              }
            },
            'application/xml': {
              'schema': {
                '$ref': '#/components/schemas/Pet'
              }
            }
          }
        },
        'UserArray': {
          'description': 'List of user object',
          'content': {
            'application/json': {
              'schema': {
                'type': 'array',
                'items': {
                  '$ref': '#/components/schemas/User'
                }
              }
            }
          }
        }
      },
      'securitySchemes': {
        'accessToken': {
          'type': 'accessToken',
          'name': 'accessToken',
          'in': 'header'
        }
      }
    }
  }

module.exports = {
  docsV1
}
