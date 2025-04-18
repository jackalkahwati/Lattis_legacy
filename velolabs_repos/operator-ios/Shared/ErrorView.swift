//
//  ErrorView.swift
//  Operator
//
//  Created by Ravil Khusainov on 03.03.2021.
//

import SwiftUI

struct ErrorView: View {
    
    let title: String
    let text: String
    @Binding var state: ViewState
    
    var body: some View {
        ZStack {
            Color.black
                .edgesIgnoringSafeArea(.all)
                .blur(radius: 300)
            VStack {
                Text(title)
                    .font(.title)
                Text(text)
                    .font(.headline)
                    .padding(.vertical)
                Button(action: { state = .screen }, label: {
                    Text("Ok")
                })
                .buttonStyle(CreateButtonStyle())
            }
            .padding()
            .background(Color.gray.opacity(0.9))
            .cornerRadius(20)
            .padding(30)
            .shadow(color: Color.black.opacity(0.1), radius: 2, x: 0, y: 2)
        }
    }
}

struct ErrorView_Previews: PreviewProvider {
    @State static var state: ViewState = .screen
    static var previews: some View {
        ErrorView(title: "Title", text: "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy try. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and ", state: $state)
    }
}
