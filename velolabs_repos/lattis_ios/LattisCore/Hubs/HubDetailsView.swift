//
//  HubDetailsView.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 31.01.2022.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import SwiftUI
import Kingfisher

struct HubDetailsView: View {
    
    @StateObject var viewModel: HubDetailsViewModel
    let dismiss: () -> Void
    
    var body: some View {
        ZStack {
            Rectangle()
                .fill(.white)
                .ignoresSafeArea(.container)
                .zIndex(-10)
            VStack {
                tableHeader
                ScrollView {
                    LazyVStack {
                        Section(header: sectionHeader) {
                            ForEach(viewModel.ports) { port in
                                Button(action: {
                                    withAnimation {
                                        viewModel.port = port
                                    }
                                }) {
                                    PortItem(port: port)
                                }
                                .foregroundColor(.black)
                            }
                        }
                    }
                }
                Button {
                    dismiss()
                } label: {
                    Text("cancel")
                        .font(.theme(weight: .medium, size: .body))
                }
                .foregroundColor(.gray)
                .padding()
            }
            .zIndex(-2)
            if let port = viewModel.port {
                PortConfirmationView(viewModel: .init(port, hub: viewModel.hub, discount: viewModel.discount, confirm: { booking in
                    dismiss()
                    viewModel.booked(port, booking)
                })) {
                    withAnimation {
                        viewModel.port = nil
                    }
                }
            }
        }
        .background(Color.white)
        .navigationBarHidden(true)
    }
    
    private var sectionHeader: some View {
        HStack {
            VStack(alignment: .leading) {
                Text(viewModel.hubName)
                    .font(.theme(weight: .bold, size: .title))
                    .foregroundColor(.black)
                    .padding(.bottom, 4)
                Text(viewModel.fleetName)
                    .font(.theme(weight: .book, size: .small))
                    .foregroundColor(.gray)
                    .padding(.bottom, 12)
                Text("select")
                    .font(.theme(weight: .medium, size: .title))
                    .foregroundColor(.black)
            }
            .padding()
            Spacer()
        }
//        .padding()
        .background(Color.white)
    }
    
    private var tableHeader: some View {
        ZStack(alignment: .bottom) {
            KFImage(viewModel.hub.imageURL)
                .resizable()
                .aspectRatio(1.0, contentMode: .fill)
                .frame(height: 200)
                .clipped()
            LinearGradient(gradient: .init(colors: [.clear, .white]), startPoint: .top, endPoint: .bottom)
                .frame(height: 100)
        }
    }
}


import UIKit

extension View {
    var ui: UIViewController {
        let controller = UIHostingController(rootView: self)
        controller.view.backgroundColor = .clear
        return controller
    }
}

extension UIViewController {
    
    @ViewBuilder var swiftUI: some View {
        UIConnector(self)
    }
}

extension UIView {
    
    @ViewBuilder var swiftUI: some View {
        UIViewConnector(self)
    }
}

struct UIViewConnector<V: UIView>: UIViewRepresentable {
    
    let view: V
    
    init(_ view: V) {
        self.view = view
    }
    
    func makeUIView(context: Context) -> some UIView {
        view
    }
    
    func updateUIView(_ uiView: UIViewType, context: Context) {
        
    }
}

struct UIConnector<V: UIViewController>: UIViewControllerRepresentable {
    let controller: V
    
    init(_ controller: V) {
        self.controller = controller
    }
    
    func makeUIViewController(context: Context) -> some UIViewController {
        controller
    }
    
    func updateUIViewController(_ uiViewController: UIViewControllerType, context: Context) {
        
    }
}

