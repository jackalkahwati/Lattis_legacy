//
//  QRCodeView.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 30/08/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import AVFoundation


public class QRCodeView<A: Codable>: UIView, AVCaptureMetadataOutputObjectsDelegate where A: Equatable {
    
    public static var string: QRCodeView<String> { .init(decode: {$0}) }
    public static func json<A: Codable>() -> QRCodeView<A> {
        .init { value in
            guard let data = value.data(using: .utf8) else {
                print("Not valid uft8 data for QR code", value)
                return nil
            }
            do {
                let r = try JSONDecoder().decode(A.self, from: data)
                return r
            } catch {
                print(error)
                return nil
            }
        }
    }
    public var completion: (State) -> () = {_ in} {
        didSet {
            completion(state)
        }
    }
    public var isScanning: Bool { session.isRunning }
    
    fileprivate let decode: (String) -> A?
    
    fileprivate let session = AVCaptureSession()
    fileprivate(set) var state: State = .noCodeFound {
        didSet {
            guard state != oldValue else { return }
            if case .code(_) = state {
                AudioServicesPlaySystemSound(SystemSoundID(kSystemSoundID_Vibrate))
            }
            completion(state)
        }
    }
    
    
    
    public init(decode: @escaping (String) -> A?) {
        self.decode = decode
        super.init(frame: .zero)
        setup()
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override public class var layerClass: AnyClass  {
        return AVCaptureVideoPreviewLayer.self
    }
    override public var layer: AVCaptureVideoPreviewLayer {
        return super.layer as! AVCaptureVideoPreviewLayer
    }
    
    public func startScan() {
        guard session.isRunning == false  else { return }
        session.startRunning()
    }
    
    public func stopScan() {
        guard session.isRunning else { return }
        session.stopRunning()
    }
    
    fileprivate func setup() {
        guard let device = AVCaptureDevice.default(for: .video),
            let input = try? AVCaptureDeviceInput(device: device),
            session.canAddInput(input) else {
                state = .setupFailed
                return
        }
        session.addInput(input)
        let output = AVCaptureMetadataOutput()
        if session.canAddOutput(output) {
            session.addOutput(output)
            
            output.setMetadataObjectsDelegate(self, queue: .main)
            output.metadataObjectTypes = [.qr, .dataMatrix]
        } else {
            state = .setupFailed
            return
        }
        
        layer.session = session
        self.layer.videoGravity = .resizeAspectFill
        session.startRunning()
    }
    
    public func metadataOutput(_ output: AVCaptureMetadataOutput, didOutput metadataObjects: [AVMetadataObject], from connection: AVCaptureConnection) {
        let result = metadataObjects.compactMap { object -> A? in
            guard let readableObject = object as? AVMetadataMachineReadableCodeObject,
                let stringValue = readableObject.stringValue,
                let r = self.decode(stringValue) else { return nil }
            return r
        }
        if result.count > 1 {
            state = .multipleCodes
        } else if let first = result.first {
            if case let .code(r) = state, r == first {
                return
            }
            state = .code(first)
        } else {
            state = .noCodeFound
        }
    }
}

public extension QRCodeView {
    enum State: Equatable {
        case noCodeFound
        case code(A)
        case multipleCodes
        case setupFailed
    }
}

