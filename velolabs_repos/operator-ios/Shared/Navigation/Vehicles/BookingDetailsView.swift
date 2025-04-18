//
//  BookingDetailsView.swift
//  Operator
//
//  Created by Ravil Khusainov on 14.06.2021.
//

import SwiftUI

struct BookingDetailsView: View {
    
    @StateObject var viewModel: BookingDetailsViewModel
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        VStack {
            Form {
                Section(header: Text("User")) {
                    FormLabel(title: "Name", value: viewModel.user.fullName)
                    FormLabel(title: "Email", value: viewModel.user.email)
                    if let phone = viewModel.user.phoneNumber {
                        FormLabel(title: "Phone number", value: phone)
                    }
                }
                Section(header: Text("Info")) {
                    FormLabel(title: "Date started", value: viewModel.booking.startedAt.asString())
                    if let date = viewModel.booking.finishedAt {
                        FormLabel(title: "Date ended", value: date.asString())
                    }
                    FormLabel(title: "Duration", value: viewModel.booking.duration)
                }
            }
            if !viewModel.isfinished {
                Button(action: {
                    viewModel.cancel {
                        presentationMode.wrappedValue.dismiss()
                    }
                }) {
                    Text("Cancel booking")
                }
                .buttonStyle(CreateButtonStyle())
                .padding([.leading, .trailing, .bottom])
            }
        }
        .navigationTitle(viewModel.vehicle.name)
    }
}

//struct BookingDetailsView_Previews: PreviewProvider {
//    static var previews: some View {
//        BookingDetailsView()
//    }
//}
