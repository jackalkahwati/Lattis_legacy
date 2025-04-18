//
//  QRScannerView.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 2022-05-04.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import SwiftUI
import QRCodeView

struct QRScannerView: View {
    
    @StateObject var viewModel: ViewModel
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        VStack(alignment: .leading, spacing: .margin/2) {
            Text("scan_qr_code_title")
                .font(.theme(weight: .medium, size: .giant))
            if let name = viewModel.asset?.name {
                Text("find_qr_code_on".localizedFormat(name))
                    .font(.theme(weight: .book, size: .title))
            }
            ZStack {
                CameraView(scanning: $viewModel.isScanning) { code in
                    viewModel.handle(code: code)
                }
                focusView
            }
            .cornerRadius(.containerCornerRadius)
            .padding(.vertical)
            controlView
        }
        .foregroundColor(.black)
        .padding()
        .background(Color.white)
    }
    
    @ViewBuilder
    fileprivate var focusView: some View {
        VStack {
            HStack {
                Image("icon_scanner_corner", bundle: .core)
                Spacer()
                Image("icon_scanner_corner", bundle: .core)
                    .rotationEffect(.degrees(90))
            }
            Spacer()
            HStack {
                Image("icon_scanner_corner", bundle: .core)
                    .rotationEffect(.degrees(-90))
                Spacer()
                Image("icon_scanner_corner", bundle: .core)
                    .rotationEffect(.degrees(180))
            }
        }
        .padding(.margin)
    }
    
    @ViewBuilder
    fileprivate var controlView: some View {
        if viewModel.torchIsAvailable {
            HStack {
                Button(action: {
                    presentationMode.wrappedValue.dismiss()
                }) {
                    Text("cancel")
                        .font(.theme(weight: .medium, size: .text))
                        .foregroundColor(.gray)
                        .frame(maxWidth: .infinity)
                }
                .padding(.trailing)
                Button(action: {
                    viewModel.toggleTorch()
                }) {
                    Image(systemName: "flashlight.on.fill")
                        .font(.theme(weight: .medium, size: .body))
                        .padding()
                        .foregroundColor(viewModel.torchEnabled ? .black : .gray)
                        .frame(maxWidth: .infinity)
                        .background(
                            Color.gray.opacity(0.2)
                        )
                        .cornerRadius(.containerCornerRadius)
                }
                .frame(height: 58)
            }
        } else {
            Button {
                presentationMode.wrappedValue.dismiss()
            } label: {
                Text("cancel")
            }
            .buttonStyle(.action())
        }
    }
}

import Combine
import Model
import AVFoundation

extension QRScannerView {
    
    final class ViewModel: ObservableObject {
        
        @Published var torchEnabled: Bool = false
        @Published var isScanning: Bool = true
        fileprivate let asset: Asset?
        fileprivate let confirm: () -> ()
        fileprivate let reader = Reader()
        
        init(_ asset: Asset?, confirm: @escaping () -> () = {}) {
            self.asset = asset
            self.confirm = confirm
        }
        
        var torchIsAvailable: Bool {
            guard let device = AVCaptureDevice.default(for: AVMediaType.video)
                else {return false}
            return device.hasTorch
        }
        
        func toggleTorch() {
            guard let device = AVCaptureDevice.default(for: AVMediaType.video)
            else {return}
            
            do {
                try device.lockForConfiguration()
                
                if device.torchMode != .on {
                    device.torchMode = .on
                    torchEnabled = true
                } else {
                    device.torchMode = .off
                    torchEnabled = false
                }
                
                device.unlockForConfiguration()
            } catch {
                print("Torch could not be used")
            }
        }
        
        func handle(code: String) -> Bool {
            if let assetCode = asset?.qrCode, let foundCode = reader.read(code: code) {
                if assetCode == foundCode {
                    isScanning = false
                    confirm()
                    return true
                } else {
                    // if code not match to current asset code, return false
                    isScanning = false
                    AppRouter.shared.showInvalidQRCodeAlert()
                    return false
                }
            }
            return false
        }
    }
    
    struct Reader {
        let urlDecoder: Decoder<URL> = .url()
        let lattisDecoder: Decoder<Bike.QRCode> = .json()
        
        func read(code: String) -> String? {
            if let result = lattisDecoder.decode(code) {
                return "\(result.qr_id)"
            }
            if let result = urlDecoder.decode(code) {
                return result.lastPathComponent
            }
            return code
        }
    }
    
    struct Decoder<T> {
        public let decode: (String) -> T?
        
        public static func json<T: Decodable>() -> Decoder<T> {
            .init { (value) -> T? in
                guard let data = value.data(using: .utf8) else {
                    print("Not valid uft8 data for QR code", value)
                    return nil
                }
                return try? JSONDecoder().decode(T.self, from: data)
            }
        }
        
        public static func url() -> Decoder<URL> { .init(decode: URL.init) }
    }
}


final class CameraView: UIView, UIViewRepresentable {
    
    let metadata: [AVMetadataObject.ObjectType]
    let found: (String) -> Bool
    @Binding var scanning: Bool
    
    init(_ metadata: [AVMetadataObject.ObjectType] = [.qr, .dataMatrix],
         scanning: Binding<Bool> = .constant(true),
         found: @escaping (String) -> Bool = {_ in false}) {
        self.metadata = metadata
        self.found = found
        self._scanning = scanning
        super.init(frame: .zero)
        self.layer.videoGravity = .resizeAspectFill
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override class var layerClass: AnyClass  {
        return AVCaptureVideoPreviewLayer.self
    }
    
    public override var layer: AVCaptureVideoPreviewLayer {
        return super.layer as! AVCaptureVideoPreviewLayer
    }
    
    func makeUIView(context: Context) -> some UIView {
        self
    }
    
    func updateUIView(_ uiView: UIViewType, context: Context) {
        context.coordinator.toggle(scanning: scanning)
    }
    
    func makeCoordinator() -> Coordinator {
        let coordinator = Coordinator(metadataTypes: metadata, found: found)
        layer.session = coordinator.session
        layer.session?.startRunning()
        return coordinator
    }
    
    final class Coordinator: NSObject, AVCaptureMetadataOutputObjectsDelegate {
        
        fileprivate let session = AVCaptureSession()
        fileprivate let found: (String) -> Bool
        
        init(metadataTypes: [AVMetadataObject.ObjectType], found: @escaping (String) -> Bool) {
            self.found = found
            super.init()
            
            guard let device = AVCaptureDevice.default(for: .video),
                let input = try? AVCaptureDeviceInput(device: device),
                session.canAddInput(input) else {
                    return
            }
            session.addInput(input)
            let output = AVCaptureMetadataOutput()
            if session.canAddOutput(output) {
                session.addOutput(output)
                
                output.setMetadataObjectsDelegate(self, queue: .main)
                output.metadataObjectTypes = metadataTypes
            } else {
                return
            }
        }
        
        func toggle(scanning: Bool) {
            if session.isRunning && !scanning {
                session.stopRunning()
            } else if !session.isRunning && scanning {
                session.startRunning()
            }
        }
        
        func metadataOutput(_ output: AVCaptureMetadataOutput, didOutput metadataObjects: [AVMetadataObject], from connection: AVCaptureConnection) {
            for meta in metadataObjects {
                guard let readableObject = meta as? AVMetadataMachineReadableCodeObject,
                    let stringValue = readableObject.stringValue else { continue }
                if found(stringValue) {
                    AudioServicesPlaySystemSound(SystemSoundID(kSystemSoundID_Vibrate))
                }
            }
        }
    }
}
