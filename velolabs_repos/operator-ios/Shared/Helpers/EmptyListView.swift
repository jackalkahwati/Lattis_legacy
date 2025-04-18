//
//  EmptyListView.swift
//  Operator
//
//  Created by Ravil Khusainov on 24.06.2021.
//

import SwiftUI

struct EmptyListView: View {
    internal init(message: LocalizedStringKey, image: String? = nil) {
        self.message = message
        self.image = image
    }
    
    let message: LocalizedStringKey
    let image: String?
    var body: some View {
        VStack {
            Spacer()
            Image(systemName: image ?? "doc.plaintext")
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(maxWidth: 100, maxHeight: 100)
                .padding(.bottom)
            Text(message)
                .font(.title)
            Spacer()
        }
        .padding()
        .foregroundColor(.accentColor)
    }
}

struct EmptyListView_Previews: PreviewProvider {
    static var previews: some View {
        EmptyListView(message: "No results")
    }
}
