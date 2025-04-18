//
//  File.swift
//  
//
//  Created by Ravil Khusainov on 16.03.2021.
//

import Vapor
import NIO
import MQTTNIO

extension Request {
    var mqtt: MQTTRunner {
        MQTTRunner(self)
    }
}

class MQTTRunner {
    private var resultPromise: EventLoopPromise<ByteBuffer>?
    private weak var req: Request?
    private var timer: Scheduled<Void>?
    private var client: MQTTClient?
    
    init(_ request: Request) {
        req = request
        guard let hostname = Environment.get("GROW_API_HOST"),
              let portSt = Environment.get("GROW_API_PORT"), let port = Int(portSt),
              let username = Environment.get("GROW_API_USERNAME"),
              let password = Environment.get("GROW_API_PASSWORD") else { return }
        client = .init(configuration:
                        .init(target: .host(hostname, port: port),
                              credentials: .init(username: username, password: password)
                        ),
                       eventLoopGroup: request.eventLoop)
    }
    
    func run(command: String, on topic: String, subscribing: String, timeout: TimeAmount = .seconds(10)) throws -> EventLoopFuture<ByteBuffer> {
        guard let client = client else { throw Failure.missingEnv }
        resultPromise = req!.eventLoop.makePromise()
        timer = req?.eventLoop.scheduleTask(in: timeout) { [weak self] in
            self?.fail(Failure.timeout)
        }
        client.addMessageListener { (client, message, context) in
            if let bytes = message.payload {
                self.success(bytes)
            } else {
                self.fail(Failure.noPayload)
            }
        }
        client.addConnectListener { (client, response, context) in
            guard response.returnCode == .accepted else {
                self.fail(Failure.connectionIssue)
                return
            }
            client.subscribe(to: subscribing).whenComplete { result in
                switch result {
                case .failure(let error):
                    self.fail(error)
                case .success(.success):
                    client.publish(topic: topic, payload: command).whenFailure { error in
                        self.fail(error)
                    }
                case .success(.failure):
                    self.fail(Failure.subRejected)
                }
            }
        }
        client.connect()
        return resultPromise!.futureResult
    }
    
    func receive(from topic: String, timeout: TimeAmount = .seconds(10)) throws -> EventLoopFuture<ByteBuffer> {
        guard let client = client else { throw Failure.missingEnv }
        resultPromise = req!.eventLoop.makePromise()
        timer = req?.eventLoop.scheduleTask(in: timeout) { [weak self] in
            self?.fail(Failure.timeout)
        }
        client.addMessageListener { (client, message, context) in
            if let bytes = message.payload {
                self.success(bytes)
            } else {
                self.fail(Failure.noPayload)
            }
        }
        client.addConnectListener { (client, response, context) in
            guard response.returnCode == .accepted else {
                self.fail(Failure.connectionIssue)
                return
            }
            client.subscribe(to: topic).whenComplete { result in
                switch result {
                case .failure(let error):
                    self.fail(error)
                case .success(.success):
                    print("Subscribed")
                case .success(.failure):
                    self.fail(Failure.subRejected)
                }
            }
        }
        client.connect()
        return resultPromise!.futureResult
    }
    
    private func success(_ bytes: ByteBuffer) {
        resultPromise?.succeed(bytes)
        client?.disconnect()
        timer?.cancel()
    }
    
    private func fail(_ error: Error) {
        resultPromise?.fail(error)
        client?.disconnect()
        timer?.cancel()
    }
    
    enum Failure: Error {
        case missingEnv
        case subRejected
        case noPayload
        case connectionIssue
        case timeout
    }
}
