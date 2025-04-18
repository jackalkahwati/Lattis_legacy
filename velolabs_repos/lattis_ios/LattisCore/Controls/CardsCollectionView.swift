//
//  CardsCollectionView.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 30/05/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit

@available(*, deprecated, message: "Remove this class")
class CardsController<CardClass: UICollectionViewCell>: NSObject, UICollectionViewDelegate, UICollectionViewDataSource  {
    
    var numberOfCards: () -> Int = { return 0 }
    var fillCard: (Int, CardClass) -> () = {_, _ in}
    var selectCard: (Int) -> () = {_ in}
    var willScrollToCardAtIndex: (Int) -> () = {_ in}
    let flowLayout: CardsFlowLayout
    let collectionView: UICollectionView
    fileprivate let cardIdentifier = "card"

    override init() {
        let layout = CardsFlowLayout()
        layout.sectionInset = .init(top: 0, left: .margin*2, bottom: .margin/2, right: .margin*2)
        layout.minimumLineSpacing = .margin/2
        layout.scrollDirection = .horizontal
        
        self.flowLayout = layout
        collectionView = UICollectionView(frame: .zero, collectionViewLayout: layout)
        collectionView.register(CardClass.self, forCellWithReuseIdentifier: cardIdentifier)
        collectionView.backgroundColor = .clear
        collectionView.showsHorizontalScrollIndicator = false
        collectionView.decelerationRate = .fast
        super.init()
        
        collectionView.delegate = self
        collectionView.dataSource = self
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func reloadData() {
        collectionView.reloadData()
    }
    
    func scrollToCard(at index: Int, animated: Bool) {
        collectionView.scrollToItem(at: .init(item: index, section: 0), at: .centeredHorizontally, animated: animated)
    }
    
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return numberOfCards()
    }
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: cardIdentifier, for: indexPath) as! CardClass
        fillCard(indexPath.item, cell)
        return cell
    }
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        selectCard(indexPath.item)
    }
    
    func scrollViewWillEndDragging(_ scrollView: UIScrollView, withVelocity velocity: CGPoint, targetContentOffset: UnsafeMutablePointer<CGPoint>) {
        var target = flowLayout.targetContentOffset(forProposedContentOffset: scrollView.contentOffset, withScrollingVelocity: velocity)
        target.x += flowLayout.itemSize.width/2
        let indexPath = collectionView.indexPathForItem(at: target)
        guard let idx = indexPath?.item else { return }
        willScrollToCardAtIndex(idx)
    }
}

