//
//  BikeInfoViewController.swift
//  Lattis
//
//  Created by Ravil Khusainov on 13/02/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit
import Localize_Swift

class BikeInfoViewController: ViewController {
    @IBOutlet var infoView: BikeInfoView!
    var interactor: BikeInfoInteractorInput!
    
    override var preferredStatusBarStyle: UIStatusBarStyle {
        return .default
    }
    
    fileprivate var sections: [Section] = []
    fileprivate var cardsSectoinIndex: Int?

    override func viewDidLoad() {
        super.viewDidLoad()

        navigationController?.isNavigationBarHidden = false
        navigationItem.leftBarButtonItem = .close(target: self, action: #selector(close(_ :)))
        
        infoView.tableView.delegate = self
        infoView.tableView.dataSource = self
        
        NotificationCenter.default.addObserver(self, selector: #selector(currentCard), name: currentCardChanged, object: nil)
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    @objc private func currentCard() {
        guard let index = cardsSectoinIndex else { return }
        infoView.tableView.reloadSections(IndexSet(integer: index), with: .automatic)
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        interactor.viewWillAppear()
    }
    
    @objc private func close(_ sender: Any) {
        navigationController?.dismiss(animated: true, completion: nil)
    }
    
    @IBAction func bookAction(_ sender: Any) {
        if let pres = navigationController?.presentingViewController?.presentingViewController {
            navigationController?.dismiss(animated: false, completion: {
                pres.dismiss(animated: true, completion: {
                    self.interactor.bookBike()
                })
            })
        } else {
            navigationController?.dismiss(animated: true, completion: {
                self.interactor.bookBike()
            })
        }
    }
}

extension BikeInfoViewController: UITableViewDataSource, UITableViewDelegate {
    func numberOfSections(in tableView: UITableView) -> Int {
        return sections.count
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return sections[section].items.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let item = sections[indexPath.section].items[indexPath.row]
        let cell = tableView.dequeueReusableCell(withIdentifier: item.identifire, for: indexPath)
        if let cell = cell as? BikeInfoCell {
            cell.model = item
        }
        if let item = item as? CreditCardCellType, case let .card(card) = item, let cell = cell as? CreditCardCell {
            cell.accessory = .select
            cell.card = card
        }
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let item = sections[indexPath.section].items[indexPath.row]
        if let item = item as? CreditCardCellType {
            switch item {
            case .add: interactor.addCreditCard()
            case .card(let card) where card.isCurrent == false: interactor.select(card: card)
            default: break
            }
        }
    }
    
    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        let labelSize = CGSize(width: view.bounds.width - 92, height: .greatestFiniteMagnitude)
        let rect = sections[section].title.boundingRect(with: labelSize, options: .usesLineFragmentOrigin, attributes: [.font: UIFont(.circularBold, size: 18)!], context: nil)
        return rect.height + 32
    }
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        let header = tableView.dequeueReusableHeaderFooterView(withIdentifier: BikeInfoSectionHeader.identifier) as! BikeInfoSectionHeader
        header.titleLabel.text = sections[section].title
        header.subtitleLabel.attributedText = sections[section].subtitle
        return header
    }
    
    func tableView(_ tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
        return 44
    }
    
    func tableView(_ tableView: UITableView, viewForFooterInSection section: Int) -> UIView? {
        return tableView.dequeueReusableHeaderFooterView(withIdentifier: BikeInfoSectionFooter.identifier)
    }
}

extension BikeInfoViewController: BikeInfoInteractorOutput {
    func update(info: Bike, cards: [CreditCard]) {
        sections = [Section(title: "bike_info_section_cost".localized(), subtitle: info.infoCost, items: [BikeInfoCell.RowModel.cost(info)])]
        if let deposit = info.depositPrice, deposit > 0 {
            sections.append(Section(title: "payment_bike_details_deposit_title".localized(), subtitle: info.infoDeposit, items: [BikeInfoCell.RowModel.deposit(info)]))
        }
           sections.append(Section(title: "bike_info_section_about".localized(), items: [BikeInfoCell.RowModel.about(info)]))
        if let network = info.network {
            sections.append(
                Section(title: "bike_info_section_network".localized(),
                        items: [BikeInfoCell.RowModel.network(network, info, showZones: interactor.showZones(for: ), openTerms: interactor.openTerms(for: ))]
            ))
        }
        if info.fleetType.isFree == false {
            var items = cards.count > 0 ? cards.map{ CreditCardCellType.card($0) } : [CreditCardCellType.empty]
            items.append(CreditCardCellType.add)
            sections.append(Section(title: "bike_info_payment_methods".localized(), items: items))
        }
        cardsSectoinIndex = sections.count - 1
        title = info.name?.uppercased()
        infoView.tableView.reloadData()
        infoView.reserveButton.isHidden = info.status != .active || info.status == .onRide
    }
}


extension BikeInfoViewController {
    struct Section {
        let title: String
        let subtitle: NSAttributedString?
        var items: [TableCellPresentable]
        init(title: String, subtitle: NSAttributedString? = nil, items: [TableCellPresentable] = []) {
            self.title = title
            self.items = items
            self.subtitle = subtitle
        }
    }
}
