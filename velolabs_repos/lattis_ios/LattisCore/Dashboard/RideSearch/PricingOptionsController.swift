//
//  PricingOptionsController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 03.08.2021.
//  Copyright © 2021 Lattis inc. All rights reserved.
//

import UIKit
import Model
import Cartography

class PricingOptionsController: NSObject {
    
    let options: [Pricing]
    var selected: Int?
    var perUse: Bool
    let perUseValue: String?
    var selectedPricing: Pricing? {
        guard let idx = selected else { return nil }
        return options[idx]
    }
    
    init(_ options: [Pricing], selected: Int?, perUseValue: String?) {
        self.options = options
        self.selected = selected
        self.perUse = selected == nil
        self.perUseValue = perUseValue
        super.init()
    }
    
    var tableHeight: CGFloat {
        let height: CGFloat = 44*CGFloat(options.count + 1) + 66
        return min(250, height)
    }
}

extension PricingOptionsController: UITableViewDelegate, UITableViewDataSource {
    func numberOfSections(in tableView: UITableView) -> Int {
        2
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        switch section {
        case 0:
            return 1
        default:
            return options.count
        }
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "cell", for: indexPath) as! PricingOptionCell
        switch indexPath.section {
        case 0:
            cell.textLabel?.text = perUseValue
            cell.isSelected = perUse
        default:
            let option = options[indexPath.row]
            cell.textLabel?.text = option.title
            cell.isSelected = selected == option.pricingOptionId
        }
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let selectedIndexPath: IndexPath = {
            var row = 0
            var section = 0
            if let sel = selected, let idx = options.firstIndex(where: {$0.pricingOptionId == sel}) {
                row = idx
                section = 1
            }
            return .init(row: row, section: section)
        }()
        switch indexPath.section {
        case 0:
            selected = nil
            perUse = true
        default:
            selected = options[indexPath.row].pricingOptionId
            perUse = false
        }
        tableView.deselectRow(at: indexPath, animated: true)
        tableView.reloadRows(at: [selectedIndexPath, indexPath], with: .automatic)
    }
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        let lable = UILabel.label(text: section == 0 ? "pay_per_use".localized() : "rental_fares".localized(), font: .theme(weight: .bold, size: .small), color: .black)
        let view = UIView()
        view.backgroundColor = .white
        view.addSubview(lable)
        constrain(lable, view) { l, v in
            l.edges == v.edges.inseted(by: 15)
        }
        return view
    }
    
    func tableView(_ tableView: UITableView, viewForFooterInSection section: Int) -> UIView? {
        UIView()
    }
}

extension Pricing {
    var title: String? {
        guard let p = price.price(for: priceCurrency) else { return nil }
        return "membership_pricing_template".localizedFormat(p, durationUnit.localizedFormat(duration))
    }
}
