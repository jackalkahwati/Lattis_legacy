//
//  FormLabel.swift
//  Operator
//
//  Created by Ravil Khusainov on 09.03.2021.
//

import SwiftUI

struct FormLabel: View {
    internal init(title: LocalizedStringKey, value: String? = nil, localizedValue: LocalizedStringKey? = nil) {
        self.title = title
        self.value = value
        self.localizedValue = localizedValue
    }
    
    
    let title: LocalizedStringKey
    let value: String?
    let localizedValue: LocalizedStringKey?
    
    var body: some View {
        HStack {
            Text(title)
                .foregroundColor(.secondary)
            Spacer()
            if let value = localizedValue {
                Text(value)
            } else if let value = value {
                Text(value)
            }
        }
    }
}

struct CustomFormLabel<Content>: View where Content: View {
    
    internal init(title: LocalizedStringKey, @ViewBuilder content: @escaping () -> Content) {
        self.title = title
        self.content = content
    }
    
    
    let title: LocalizedStringKey
    let content: () -> Content
    
    var body: some View {
        HStack {
            Text(title)
                .foregroundColor(.secondary)
            Spacer()
            content()
        }
    }
}
