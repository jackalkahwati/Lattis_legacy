//
//  SignInView.swift
//  Operator
//
//  Created by Ravil Khusainov on 24.02.2021.
//

import SwiftUI

struct SignInView: View {
    
    @StateObject var logic: SignInLogicController
    
    var body: some View {
        VStack {
            Spacer()
            VStack {
            SplashItem()
            VStack {
                HStack {
                    Image(systemName: "envelope")
                        .frame(width: 22, height: 22)
                    TextField("email", text: $logic.email)
                        .padding(.vertical)
                        .keyboardType(.emailAddress)
                        .autocapitalization(.none)
                        .disableAutocorrection(true)
                }
                HStack {
                    Image(systemName: "key")
                        .frame(width: 22, height: 22)
                    SecureField("password", text: $logic.password)
                        .padding(.vertical)
                }
                Button(action: logic.login, label: {
                    Text("login")
                })
                .buttonStyle(CreateButtonStyle())
                .disabled(!logic.validate())
                .padding(.vertical)
                .contextMenu {
                    #if DEBUG
                    debugMenu
                    #endif
                }
            }
            .viewState($logic.state)
            }
            .frame(maxWidth: 500, maxHeight: 500)
            Spacer()
        }
        .cornerRadius(10)
        .padding()
        .background(
            Color.background
                .ignoresSafeArea()
        )
    }
    
    private var debugMenu: some View {
        Group {
            #if DEBUG
            ForEach([FleetOperator.Demo].dummy) { oper in
                Button(action: { logic.login(oper) }, label: {
                    Text(oper.name)
                })
            }
            #endif
        }
    }
}

struct SignInView_Previews: PreviewProvider {
    static var previews: some View {
        SignInView(logic: .init(UserSettings()))
    }
}

