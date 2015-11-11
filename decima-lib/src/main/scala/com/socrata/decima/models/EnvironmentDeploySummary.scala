package com.socrata.decima.models

case class EnvironmentDeploySummary(service: String,
                                    serviceAlias: String,
                                    environment: String,
                                    parityWithReference: Boolean,
                                    referenceEnvironment: String,
                                    deploy: Deploy,
                                    parityStatus: String)
