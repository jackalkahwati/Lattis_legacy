//
//  EditView.swift
//  Operator
//
//  Created by Ravil Khusainov on 19.03.2021.
//

import SwiftUI

struct EditView: View {
    
    @Environment(\.presentationMode) var presentationMode
    @State fileprivate var text: String = ""
    fileprivate let original: String
    fileprivate let title: String
    fileprivate let saving: (String) -> Void
    
    init(_ text: String? = nil, title: String, completion: @escaping (String) -> Void) {
        self.original = text ?? ""
        self.saving = completion
        self.title = title
    }
    
    var body: some View {
        NavigationView {
            VStack {
                TextEditor(text: $text)
                    .padding()
                Button(action: {
                    saving(text)
                    presentationMode.wrappedValue.dismiss()
                }) {
                    Text("save")
                }
                .buttonStyle(CreateButtonStyle())
                .padding()
                .disabled(text == original)
            }
            .navigationTitle(title)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(action: { presentationMode.wrappedValue.dismiss() }) {
                        Text("cancel")
                    }
                    .foregroundColor(.white)
                }
            }
        }
        .onAppear {
            text = original
        }
    }
}

struct EditView_Previews: PreviewProvider {
    static var previews: some View {
        EditView(nil, title: "Notes") { _ in}
    }
}
