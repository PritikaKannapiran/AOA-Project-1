import pandas as pd
import matplotlib.pyplot as plt
import numpy as np

df = pd.read_csv('dc_analysis_results.csv')

fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(15, 6))

ax1.errorbar(df['GridSize'], df['ActualTime'], yerr=df['StdDev'], 
             marker='o', capsize=5, label='Actual Performance', linewidth=2, color='blue')

theoretical_normalized = df['TheoreticalComplexity'] * (df['ActualTime'].iloc[0] / df['TheoreticalComplexity'].iloc[0])
ax1.plot(df['GridSize'], theoretical_normalized, 
         linestyle='--', label='Theoretical O(n² log n)', linewidth=2, color='red')

ax1.set_xlabel('Grid Size (n)', fontsize=12)
ax1.set_ylabel('Running Time (ms)', fontsize=12)
ax1.set_title('Divide & Conquer: Actual vs Theoretical Performance', fontsize=14, fontweight='bold')
ax1.grid(True, alpha=0.3)
ax1.legend()

ax2.loglog(df['GridSize'], df['ActualComparisons'], 
           marker='o', label='Actual Comparisons', linewidth=2, color='blue')
ax2.loglog(df['GridSize'], df['TheoreticalComplexity'], 
           linestyle='--', label='Theoretical O(n² log n)', linewidth=2, color='red')

ax2.set_xlabel('Grid Size (n)', fontsize=12)
ax2.set_ylabel('Number of Operations (log scale)', fontsize=12)
ax2.set_title('Complexity Analysis: Actual vs Theoretical', fontsize=14, fontweight='bold')
ax2.grid(True, alpha=0.3, which='both')
ax2.legend()

efficiency = (df['ActualComparisons'] / df['TheoreticalComplexity'] * 100).mean()
plt.figtext(0.5, 0.02, 
    f'Average Algorithm Efficiency: {efficiency:.1f}% of theoretical maximum\n' +
    'Divide & Conquer shows consistent O(n² log n) scaling behavior', 
    ha='center', fontsize=10, bbox=dict(facecolor='lightgray', alpha=0.8))

plt.tight_layout()
plt.subplots_adjust(bottom=0.15)
plt.savefig('dc_complexity_analysis.png', dpi=300, bbox_inches='tight')
print('Divide & Conquer complexity analysis plot saved as dc_complexity_analysis.png')
plt.show()
