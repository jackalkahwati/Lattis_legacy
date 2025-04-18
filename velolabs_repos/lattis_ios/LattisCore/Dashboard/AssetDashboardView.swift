//
//  RentalDashboardView.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 07.03.2022.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import SwiftUI

struct AssetDashboardView: View {
    
    @StateObject var viewModel: ViewModel
    
    let touchView: UIView
    var body: some View {
        ZStack {
            touchView.swiftUI
            VStack(alignment: .leading) {
                menuButton
                Spacer()
                ZStack {
                    switch viewModel.card {
                    case .booking(let booking):
                        AssetBookingView(viewModel: .init(viewModel.asset, card: $viewModel.card, booking: booking))
                            .transition(.move(edge: .bottom))
                    case .rental(let trip):
                        AssetRentalView(viewModel: .init(trip: trip, asset: viewModel.asset, alert: $viewModel.alert, card: $viewModel.card))
                            .transition(.move(edge: .bottom))
                    case .parking(let spot):
                        ParkingCardView(spot: spot, previousCard: viewModel.lastCard, card: $viewModel.card)
                            .transition(.move(edge: .bottom))
                    case .summary(let trip):
                        RentalSummaryView(viewModel: .init(trip: trip, asset: viewModel.asset, card: $viewModel.card))
                            .transition(.move(edge: .bottom))
                    case .none, .dismiss:
                        EmptyView()
                            .frame(maxWidth: .infinity)
                    }
                }
            }
            if let alert = viewModel.alert {
                CustomAlertView(view(for: alert))
            }
        }
        .animation(.spring(), value: viewModel.card)
        .onAppear {
            viewModel.animateAppearance()
        }
        .onChange(of: viewModel.card) { newValue in
            guard newValue == .dismiss else { return }
            viewModel.goBack()
        }
    }
    
    @ViewBuilder
    fileprivate var menuButton: some View {
        Button(action: MenuUI.shared.showMenu) {
            Image("icon_menu", bundle: .core)
                .font(.title)
                .padding()
                .background(
                    Circle().fill(.white)
                        .shadow(radius: 5, y: 4)
                )
        }
        .padding(.top, .margin/2)
        .padding(.leading, .margin/2)
    }
    
    @ViewBuilder
    fileprivate func view(for alert: Alert) -> some View {
        switch alert {
        case .equipment(let feedback):
            switch feedback {
            case .code(let code):
                VStack {
                    HStack {
                        Spacer()
                        Text(code)
                            .font(.theme(weight: .medium, size: .mighty))
                        Spacer()
                    }
                    Button {
                        withAnimation(.spring()) {
                            viewModel.alert = nil
                        }
                    } label: {
                        Text("ok")
                    }
                    .buttonStyle(ActionButtonStyle(isLoading: .constant(false)))
                }
            case .success:
                VStack {
                    HStack {
                        Spacer()
                        Text("success")
                            .font(.theme(weight: .medium, size: .giant))
                        Spacer()
                    }
                    Button {
                        withAnimation(.spring()) {
                            viewModel.alert = nil
                        }
                    } label: {
                        Text("ok")
                    }
                    .buttonStyle(ActionButtonStyle(isLoading: .constant(false)))
                }
            case .unsuccessful:
                VStack {
                    HStack {
                        Spacer()
                        Text("unsuccessful")
                            .font(.theme(weight: .medium, size: .giant))
                        Spacer()
                    }
                    Button {
                        withAnimation(.spring()) {
                            viewModel.alert = nil
                        }
                    } label: {
                        Text("ok")
                    }
                    .buttonStyle(ActionButtonStyle(isLoading: .constant(false)))
                }
            }
        }
    }
}

import UIKit

final class AssetDashboardViewController: UIViewController {
    
    weak var map: MapRepresentable!
    let passView = PassthroughView()
    let viewModel: AssetDashboardView.ViewModel
    
    init(_ map: MapRepresentable?, viewModel: AssetDashboardView.ViewModel) {
        self.map = map
        self.viewModel = viewModel
        super.init(nibName: nil, bundle: nil)
        passView.targetView = map?.mapView
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func loadView() {
        view = passView
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        let transparent = UIView()
        let sui = AssetDashboardView(viewModel: self.viewModel, touchView: transparent).ui
        sui.willMove(toParent: self)
        view.addSubview(sui.view)
        sui.didMove(toParent: self)

        passView.touchTransparentView = transparent
        
        sui.view.translatesAutoresizingMaskIntoConstraints = false
        
        NSLayoutConstraint.activate([
            sui.view.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor),
            sui.view.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            sui.view.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            sui.view.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor)
        ])
        
        let footer = UIView()
        footer.translatesAutoresizingMaskIntoConstraints = false
        footer.backgroundColor = .white
        view.addSubview(footer)
        
        NSLayoutConstraint.activate([
            footer.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor),
            footer.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            footer.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            footer.bottomAnchor.constraint(equalTo: view.bottomAnchor)
        ])
    }
}

extension Trip: Equatable {
    static func == (lhs: Trip, rhs: Trip) -> Bool {
        lhs.tripId == rhs.tripId
    }
}

struct CustomAlertView<C: View>: View {
    
    let content: C
    
    init(_ content: C) {
        self.content = content
    }
    
    var body: some View {
        ZStack {
            Rectangle()
                .fill(.black.opacity(0.4))
            VStack {
                Spacer()
                content
                .padding()
                .background(
                    RoundedRectangle(cornerRadius: .containerCornerRadius)
                        .fill(.white)
                )
                .padding(.horizontal)
                Spacer()
            }
        }
    }
}

