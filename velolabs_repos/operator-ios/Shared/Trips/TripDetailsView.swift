//
//  TripDetailsView.swift
//  Operator
//
//  Created by Ravil Khusainov on 04.04.2021.
//

import SwiftUI

struct TripDetailsView: View {
    
    @StateObject var viewModel: ViewModel
    @Environment(\.presentationMode) var presentationMode
    @State var sheetState: SheetState?
    @State var blockingUser: Bool = false
    
    var body: some View {
        VStack {
            Form {
                Section(header: Text("user")) {
                    FormLabel(title: "name", value: viewModel.trip.user.fullName)
                    CustomFormLabel(title: "email") {
                        Menu(viewModel.trip.user.email) {
                            Button(action: {
                                guard MailView.canSendEmail else { return }
                                sheetState = .mail
                            }, label: {
                                Label("send-email", systemImage: "at")
                            })
                            Button(action: { viewModel.copy(value: viewModel.trip.user.fullName) }, label: {
                                Label("copy", systemImage: "doc.on.clipboard.fill")
                            })
                        }
                    }
                    if let phone = viewModel.phoneNumber {
                        CustomFormLabel(title: "phone-number") {
                            Menu(phone) {
                                Button(action: { viewModel.call(phoneNumber: phone) }, label: {
                                    Label("call", systemImage: "phone.fill")
                                })
                                Button(action: {
                                    guard MessageView.canSendMessage else { return }
                                    sheetState = .message
                                }, label: {
                                    Label("message", systemImage: "message.fill")
                                })
                                Button(action: { viewModel.copy(value: phone) }, label: {
                                    Label("copy", systemImage: "doc.on.clipboard.fill")
                                })
                            }
                        }
                    }
                    if let userBlocked = viewModel.blockedUser {
                        Button(action: {
                            blockingUser = true
                        }, label: {
                            Text(userBlocked ? "user-is-blocked" : "block-user")
                        })
//                        .disabled(userBlocked)
                    }
                }
                Section(header: Text("info")) {
                    FormLabel(title: "date-started", value: viewModel.trip.createdAt.asString())
                    if let date = viewModel.trip.endedAt {
                        FormLabel(title: "date-ended", value: date.asString())
                    }
                    FormLabel(title: "duration", value: viewModel.trip.duration)
                    if let address = viewModel.trip.startAddress {
                        FormLabel(title: "from", value: address)
                    }
                    if let address = viewModel.trip.endAddress {
                        FormLabel(title: "to", value: address)
                    }
                }
            }
            if !viewModel.isEnded {
                Button(action: {
                    viewModel.endTrip {
                        presentationMode.wrappedValue.dismiss()
                    }
                }) {
                    Text("end-trip")
                }
                .buttonStyle(CreateButtonStyle())
                .padding([.leading, .trailing, .bottom])
            }
        }
        .sheet(item: $sheetState, content: { state in
            switch state {
            case .mail:
                MailView(recepients: [viewModel.trip.user.email])
                    .ignoresSafeArea()
            case .message:
                MessageView(recepients: [viewModel.trip.user.phoneNumber!])
                    .ignoresSafeArea()
            }
        })
        .actionSheet(isPresented: $blockingUser, content: {
            ActionSheet(title: Text("please-confirm"), buttons: [
                .destructive(Text(viewModel.blockedUser! ? "unblock" : "block"), action: viewModel.toggleUserLock),
                .cancel()
            ])
        })
        .onAppear(perform: viewModel.checkIfUserBlocked)
        .viewState($viewModel.viewState)
        .navigationTitle(viewModel.vehicle.name)
    }
}

extension TripDetailsView {
    enum SheetState: Identifiable {
        case mail
        case message
        
        var id: Self { self }
    }
}

//struct TripDetailsView_Previews: PreviewProvider {
//    static var previews: some View {
//        TripDetailsView()
//    }
//}
