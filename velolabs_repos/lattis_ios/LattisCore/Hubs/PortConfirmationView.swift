//
//  PortConfirmationView.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 17.02.2022.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import SwiftUI
import Atributika
import SafariServices

struct PortConfirmationView: View {
    
    @StateObject var viewModel: ViewModel
    let deselect: () -> Void
    
    var body: some View {
        ZStack(alignment: .bottom) {
            LinearGradient(gradient: .init(colors: [.clear, .clear, .black.opacity(0.5), .black.opacity(0.3), .white]), startPoint: .top, endPoint: .bottom)
                .onTapGesture {
                    dismiss()
                }
                .zIndex(-3)
            switch viewModel.cardState {
            case .none:
                EmptyView()
            case .confirmation:
                confirmationCard
                    .transition(.move(edge: .bottom).combined(with: .opacity))
            case .pricing:
                SelectPricingView(viewModel: .init(viewModel.pricingOptions, perUse: viewModel.perUsePrice, confirmed: $viewModel.pricing)) {
                    viewModel.cardState = .confirmation
                }
                .transition(.move(edge: .bottom).combined(with: .opacity))
            }

            switch viewModel.phoneState {
            case.profile:
                UIViewController.navigation(ProfileViewController(false)).swiftUI
            case .add:
                CustomAlertView(
                    PhoneNumberView(addPhoneAction: {
                        viewModel.phoneState = .profile
                    }, cancelAction: {
                        viewModel.phoneState = .none
                    })
                )
            default:
                EmptyView()
            }
        }
        .background(Color.clear)
        .onAppear {
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
                viewModel.cardState = .confirmation
            }
        }
        .sheet(item: $viewModel.modalScreen, content: { modal in
            switch modal {
            case .paymentMethods:
                UIViewController.navigation(PaymentMethodsViewController(logic: .init(fleet: viewModel.hub.fleet))).swiftUI
            case .safari(let url):
                SafariView(url: url)
            case .tutorial:
                TutorialManager.shared.controller {
                    self.viewModel.confirm()
                }
                .swiftUI
            }
        })
        .animation(.spring(), value: viewModel.cardState)
        .animation(.spring(), value: viewModel.phoneState)
        .animation(.spring(), value: viewModel.hint)
    }
    
    fileprivate func dismiss() {
        viewModel.cardState = .none
        viewModel.phoneState = .none
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.2, execute: deselect)
    }
    
    fileprivate var confirmationCard: some View {
        ZStack(alignment: .init(horizontal: .leading, vertical: .cardTop)) {
            if let _ = viewModel.hint {
                HintView(hint: $viewModel.hint, padding: .margin/2, permanent: false)
            }
            VStack(alignment: .leading, spacing: .margin/2) {
                HStack(alignment: .top) {
                    VStack(alignment: .leading) {
                        Text(viewModel.hubName)
                            .font(.theme(weight: .bold, size: .title))
                            .foregroundColor(.black)
                            .padding(.bottom, 4)
                        Text(viewModel.fleetName)
                            .font(.theme(weight: .book, size: .small))
                            .foregroundColor(.gray)
                    }
                    Spacer()
                    VStack(alignment: .trailing) {
                        Text("#\(viewModel.port.portNumber ?? 0)")
                            .font(.theme(weight: .bold, size: .giant))
                        Text("parking")
                            .font(.theme(weight: .bold, size: .small))
                    }
                }
                Capsule()
                    .foregroundColor(.gray.opacity(0.3))
                    .frame(height: 1)
                Text("ride_cost")
                    .font(.theme(weight: .medium, size: .body))
                    .foregroundColor(.gray)
                    .padding(.bottom)
                costsView
                FlowControlView(negative: ("cancel", dismiss), positive: ("confirm", {
                    viewModel.confirm()
                }), isLoading: $viewModel.isLoading)
                .padding(.top)
            }
            .foregroundColor(.black)
            .padding()
            .background(
                Color.white
                    .cornerRadius(.containerCornerRadius, corners: [.topLeft, .topRight])
            )
            .alignmentGuide(.cardTop) { d in
                d[VerticalAlignment.top]
            }
        }
    }
    
    @ViewBuilder
    fileprivate var costsView: some View {
        row("bike_detail_label_price", value: priceView)
        if let unlock = viewModel.unlockFee {
            row("unlock_fee", value: Text(unlock))
        }
        if let surcharge = viewModel.surchargeFee {
            row("surcharge", value: Text(surcharge))
            if let text = viewModel.settings?.surchargeDescription {
                Text(text)
                    .font(.theme(weight: .light, size: .small))
                    .padding(.top, -6)
            }
        }
        if let parking = viewModel.settings?.parkingFee {
            row("bike_detail_label_parking_fee", value: Text(parking))
            Text("bike_detail_label_parking_fee_warning")
                .font(.theme(weight: .light, size: .small))
                .padding(.top, -6)
        }
        if let preauth = viewModel.settings?.preauthPrice {
            row("preauthorization", value: Text(preauth))
            Text("preauthorization_description")
                .font(.theme(weight: .light, size: .small))
                .padding(.top, -6)
        }
        if let membership = viewModel.discount {
            row("membership", value: Text("membership_discount_template".localizedFormat(membership.string())))
        }
        if let promo = viewModel.hub.promotions?.first {
            row("promo_code", value: Text("membership_discount_template".localizedFormat(promo.amount.string())))
        }
        if let card = viewModel.cardNumber {
            row("payment", value: Button(action: { viewModel.modalScreen = .paymentMethods }) {
                HStack {
                    Label(card, systemImage: "creditcard")
                    Image(systemName: "chevron.right")
                }
            })
        }
        Capsule()
            .foregroundColor(.gray.opacity(0.3))
            .frame(height: 1)
        if let link = viewModel.hub.fleet.legal {
            AttributedLabel.url(text: "bike_details_terms_policy".localizedFormat(link)) { url in
                viewModel.modalScreen = .safari(url)
            }.swiftUI
                .frame(maxHeight: 36)
        }
    }
    
    @ViewBuilder
    fileprivate func row<V: View>(_ title: LocalizedStringKey, value: V) -> some View {
        HStack {
            Text(title)
                .font(.theme(weight: .medium, size: .text))
            Spacer()
            value
                .font(.theme(weight: .bold, size: .text))
        }
        .foregroundColor(.black)
    }
    
    @ViewBuilder
    fileprivate var priceView: some View {
        if !viewModel.pricingOptions.isEmpty {
            Button(action: {
                viewModel.cardState = .pricing
            }) {
                HStack {
                    Text(viewModel.pricingOptinsFare)
                        .multilineTextAlignment(.trailing)
                    Image(systemName: "chevron.right")
                }
            }
        } else {
            Text(viewModel.baseFare)
                .multilineTextAlignment(.trailing)
        }
    }
}

extension AttributedLabel {
        static func url(text: String? = nil, open: @escaping (URL) -> ()) -> AttributedLabel {
        let label = AttributedLabel()
        label.numberOfLines = 0
        label.textAlignment = .center
        let all = Style.font(.theme(weight: .light, size: .small))
            .foregroundColor(.black)
        let link = Style("a")
            .foregroundColor(.black, .normal)
            .foregroundColor(.blue, .highlighted)
            .underlineStyle(.single)
        label.attributedText = (text ?? UITheme.theme.legal)
            .style(tags: link)
            .styleLinks(link)
            .styleAll(all)
        label.onClick = { label, detection in
            guard case let .tag(tag) = detection.type,
                let url = tag.url else { return }
            open(url)
        }
        return label
    }
}

struct SafariView: UIViewControllerRepresentable {

    let url: URL

    func makeUIViewController(context: UIViewControllerRepresentableContext<SafariView>) -> SFSafariViewController {
        return SFSafariViewController(url: url)
    }

    func updateUIViewController(_ uiViewController: SFSafariViewController, context: UIViewControllerRepresentableContext<SafariView>) {

    }
}

               
struct ProfileView: UIViewControllerRepresentable {

    func makeUIViewController(context: UIViewControllerRepresentableContext<ProfileView>) -> ProfileViewController {
        let profile = ProfileViewController(false)
        return profile
    }
    
    func updateUIViewController(_ uiViewController: ProfileViewController, context: UIViewControllerRepresentableContext<ProfileView>) {}
}


struct PhoneNumberView: View {
    
    var addPhoneAction: (() -> Void)?
    var cancelAction: (() -> Void)?
         
    var body: some View {
        
        VStack(alignment: .center) {
            VStack {
                Text("label_note".localized())
                    .font(.theme(weight: .bold, size: .title))
                    .padding(.vertical, .margin/2)
                    
                Text("mandatory_phone_text".localized())
                    .font(.theme(weight: .light, size: .body))
                    .padding(.leading)
                
                Divider()
                
                Button {
                    addPhoneAction!()
                } label: {
                    Text("mandatory_phone_action".localized())
                }
                .buttonStyle(.action())
                .padding(10)

                Button(action: {
                    cancelAction!()
                }) {
                    Text( "cancel".localized())
                        .font(.theme(weight: .bold, size: .title))
                        .foregroundColor(.gray)
                        .frame(maxWidth: .infinity)
                }
                .padding(10)
                .buttonStyle(.plain)
            }
            .font(.theme(weight: .bold, size: .text))
        }
        .padding()
    }
 }
