//
//  TagButton.swift
//  BLUF
//
//  Created by Ravil Khusainov on 18.08.2020.
//

import SwiftUI

struct TagButton: View {
    
    let title: String
    let value: String
    let remove: () -> Void
    
    var body: some View {
//        GeometryReader { geometry in
            HStack(spacing: 0) {
                VStack {
                    Spacer()
                Text(title)
                    Spacer()
            }
                    .padding(.horizontal, 10)
                    .background(Color.blue)
                Text(value)
                    .fontWeight(.bold)
                    .padding(.horizontal, 10)
                    .padding(.vertical, 5)
                Button(action: remove, label: {
                    Image(systemName: "xmark.square")
                })
                .padding(.trailing, 10)
            }
            
            .background(Color.lightBlue)
            .cornerRadius(5)
            .foregroundColor(.white)
//        }
    }
}

struct TagButton_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            ScrollView {
                TagButton(title: "Tagiok", value: "sdfsdfsdsfjsldfjskfdsjdkfjskdfjskdfljsfjksfsd") {
                    
                }
            }
            .padding()
        }
    }
}
