//
//  SelectPricingView.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 21.02.2022.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import SwiftUI

struct SelectPricingView: View {
    
    @StateObject var viewModel: ViewModel
    let dismiss: () -> Void
    
    var body: some View {
        VStack(alignment: .leading) {
            Text("select_pricing")
                .font(.theme(weight: .medium, size: .title))
                .padding(.bottom)
            VStack(alignment: .leading) {
                Section(header: Text("pay_per_use").font(.theme(weight: .bold, size: .small)).padding(.bottom)) {
                    Button(action: {
                        viewModel.selected = viewModel.perUse
                    }) {
                        row(viewModel.perUse, selected: viewModel.selected == viewModel.perUse)
                    }
                    .padding(.bottom)
                }
                Section(header: Text("rental_fares").font(.theme(weight: .bold, size: .small)).padding(.bottom)) {
                    ForEach(viewModel.options) { option in
                        Button(action: {
                            viewModel.selected = option
                        }) {
                            row(option, selected: viewModel.selected == option)
                        }
                        .padding(.bottom)
                    }
                }
            }
            FlowControlView(negative: ("cancel", dismiss), positive: ("confirm", {
                viewModel.confirm()
                dismiss()
            }))
        }
        .foregroundColor(.black)
        .padding()
        .background(
            Color.white
                .cornerRadius(.containerCornerRadius, corners: [.topLeft, .topRight])
        )
    }
    
    fileprivate func row(_ option: Pricing.Option, selected: Bool) -> some View {
        HStack {
            Text(option.title)
                .multilineTextAlignment(.trailing)
            Spacer()
            if selected {
                Image(systemName: "checkmark")
                    .font(.title3)
            }
        }
        .foregroundColor(.black)
        .font(.theme(weight: .bold, size: .body))
    }
}

import Model
import Combine

extension SelectPricingView {
    
    @MainActor
    final class ViewModel: ObservableObject {
        
        @Published var selected: Pricing.Option
        @Binding var confirmed: Pricing.Option
        
        let options: [Pricing.Option]
        let perUse: Pricing.Option
                
        init(_ pricing: [Pricing], perUse: String, confirmed: Binding<Pricing.Option>) {
            self.options = pricing.map{.pricing($0)}
            self.perUse = .perUse(perUse)
            self._confirmed = confirmed
            selected = confirmed.wrappedValue == .none ? .perUse(perUse) : confirmed.wrappedValue
        }
        
        func confirm() {
            confirmed = selected
        }
    }
}

extension Pricing {
    enum Option: Identifiable, Equatable {
        case none
        case perUse(String)
        case pricing(Pricing)
        
        var id: Int {
            switch self {
            case .none:
                return 0
            case .perUse:
                return 1
            case .pricing(let price):
                return price.pricingOptionId
            }
        }
        
        var title: String {
            switch self {
            case .none:
                return "select_pricing".localized()
            case .pricing(let pricing):
                return pricing.title ?? "invalid"
            case .perUse(let title):
                return title
            }
        }
        
        static func == (lhs: Pricing.Option, rhs: Pricing.Option) -> Bool {
            lhs.id == rhs.id
        }
    }
}
