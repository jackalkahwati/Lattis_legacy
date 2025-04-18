//
//  TagView.swift
//  Operator
//
//  Created by Ravil Khusainov on 26.04.2021.
//

import SwiftUI

protocol TagRepresentable {
    var title: LocalizedStringKey { get }
    var value: String { get }
    var localizedValue: LocalizedStringKey? { get }
}

extension Vehicle {
    enum SearchTag {
        case name(String)
        case status(Status)
    }
}

extension Vehicle.SearchTag: TagRepresentable, Identifiable {
    var title: LocalizedStringKey {
        switch self {
        case .name:
            return "name"
        case .status:
            return "status"
        }
    }
    
    var value: String {
        switch self {
        case .name(let name):
            return name
        case .status(let status):
            return status.rawValue
        }
    }
    
    var localizedValue: LocalizedStringKey? {
        switch self {
        case .status(let status):
            return status.displayValue
        default:
            return nil
        }
    }
    
    var id: String { "\(title)" + value }
}

struct TagView: View {
    let tag: TagRepresentable
    let action: () -> Void
    
    var body: some View {
        Button(action: action, label: {
            HStack {
                Text(tag.title).foregroundColor(.primary).padding(.leading, 6)
                valueView.foregroundColor(.white).padding(6)
                    .background(
                        Color.accentColor
                    )
                    .cornerRadius(5)
            }
            .overlay(
                RoundedRectangle(cornerRadius: 5)
                    .stroke(Color.accentColor)
            )
            .background(Color.background)
//            .font(.body)
        })
    }
    
    private var valueView: some View {
        if let localized = tag.localizedValue {
            return Text(localized)
        } else {
            return Text(tag.value)
        }
    }
}

struct TagView_Previews: PreviewProvider {
    static var previews: some View {
        TagView(tag: Vehicle.SearchTag.name("Wolverine"), action: {})
    }
}
