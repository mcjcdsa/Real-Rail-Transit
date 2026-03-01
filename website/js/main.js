// 复制链接功能
document.addEventListener('DOMContentLoaded', function() {
    // 查找所有分享链接按钮
    const shareLinks = document.querySelectorAll('.share-link');
    
    shareLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            
            // 获取当前页面URL
            const url = window.location.href;
            
            // 复制到剪贴板
            if (navigator.clipboard && navigator.clipboard.writeText) {
                navigator.clipboard.writeText(url).then(() => {
                    // 显示成功提示
                    const originalText = link.textContent;
                    link.textContent = '已复制！';
                    link.style.color = '#10b981';
                    
                    setTimeout(() => {
                        link.textContent = originalText;
                        link.style.color = '';
                    }, 2000);
                }).catch(err => {
                    console.error('复制失败:', err);
                    fallbackCopy(url, link);
                });
            } else {
                // 降级方案
                fallbackCopy(url, link);
            }
        });
    });
});

// 降级复制方案
function fallbackCopy(text, element) {
    const textArea = document.createElement('textarea');
    textArea.value = text;
    textArea.style.position = 'fixed';
    textArea.style.left = '-999999px';
    document.body.appendChild(textArea);
    textArea.focus();
    textArea.select();
    
    try {
        document.execCommand('copy');
        const originalText = element.textContent;
        element.textContent = '已复制！';
        element.style.color = '#10b981';
        
        setTimeout(() => {
            element.textContent = originalText;
            element.style.color = '';
        }, 2000);
    } catch (err) {
        console.error('复制失败:', err);
        alert('无法复制链接，请手动复制：' + text);
    }
    
    document.body.removeChild(textArea);
}

// 平滑滚动
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        e.preventDefault();
        const target = document.querySelector(this.getAttribute('href'));
        if (target) {
            target.scrollIntoView({
                behavior: 'smooth',
                block: 'start'
            });
        }
    });
});

