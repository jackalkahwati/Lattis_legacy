//
//  KisiDetailFormView.swift
//  Operator
//
//  Created by Ravil Khusainov on 24.11.2021.
//

import SwiftUI


struct KisiDetailFormView: View {
    
    @EnvironmentObject var viewModel: KisiDetailFormViewModel
    
    var body: some View {
        Group {
            FormLabel(title: "vendor", value: viewModel.thing.metadata.vendor)
            FormLabel(title: "identifier", value: viewModel.thing.metadata.key)
            if let name = viewModel.device?.name {
                FormLabel(title: "name", value: name)
            }
            if let online = viewModel.device?.online {
                FormLabel(title: "status", localizedValue: online ? "online" : "offline")
            }
            if let online = viewModel.device?.online, online {
                Button(action: viewModel.unlock) {
                    Text("unlock")
                }
                .progress(indicator: viewModel.unlocking)
            }
        }
    }
}
